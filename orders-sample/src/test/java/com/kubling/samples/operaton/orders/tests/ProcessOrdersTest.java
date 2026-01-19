package com.kubling.samples.operaton.orders.tests;

import com.kubling.samples.operaton.orders.AbstractOrdersFlowIntegrationTest;
import com.kubling.samples.operaton.support.AbstractContainersConfig;
import com.kubling.samples.operaton.support.DataPlaneContainersConfig;
import com.kubling.samples.operaton.support.OrdersContainersConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.operaton.bpm.engine.history.HistoricProcessInstance;
import org.operaton.bpm.engine.history.HistoricVariableInstance;
import org.operaton.bpm.engine.impl.pvm.PvmException;
import org.operaton.bpm.engine.runtime.ProcessInstance;
import org.operaton.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.operaton.bpm.engine.variable.Variables;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
public class ProcessOrdersTest extends AbstractOrdersFlowIntegrationTest {

    @BeforeAll
    static void setupEnvironment() throws SQLException {
        System.setProperty("KUBLING_OPERATON_PORT", String.valueOf(DataPlaneContainersConfig.getKublingPort()));
        System.setProperty(
                "KUBLING_ORDERS_PORT",
                String.valueOf(
                        OrdersContainersConfig.getKublingPort(
                                AbstractContainersConfig.SoftTransactionStrategy.IMMEDIATE_OPERATION)
                )
        );
        populateOrdersData();
    }

    @AfterAll
    static void shutdownEnvironment() {
        OrdersContainersConfig.shutdown();
        DataPlaneContainersConfig.shutdown();
    }

    @Test
    @Order(1)
    void shouldStartProcessAndWriteHistory() throws SQLException {

        ProcessInstance instance = processRunnerService.startOrderProcess(
                "order-kubling",
                Map.of(
                        "mustFail", Boolean.FALSE,
                        "customer", RandomUtils.insecure().randomInt(),
                        "sku", "SKU-006",
                        "qty", 8,
                        "unitPrice", Variables.objectValue(new BigDecimal(RandomUtils.insecure().randomDouble(1, 1500)))
                )
        );
        assertThat(instance).isNotNull();

        BpmnAwareTests.assertThat(instance).isEnded();

        HistoricProcessInstance history = historyService
                .createHistoricProcessInstanceQuery()
                .processInstanceId(instance.getId())
                .singleResult();

        assertThat(history).isNotNull();
        assertThat(history.getProcessDefinitionKey()).isEqualTo("order-kubling");

        List<HistoricVariableInstance> historicVars =
                historyService.createHistoricVariableInstanceQuery()
                        .processInstanceId(instance.getId())
                        .list();

        historicVars.forEach(v ->
                log.debug("{} = {}", v.getName(), v.getValue())
        );

        Map<String, Object> vars = historicVars.stream()
                .collect(Collectors.toMap(
                        HistoricVariableInstance::getName,
                        HistoricVariableInstance::getValue
                ));

        var rows = runQueryInOrders("SELECT * FROM orders");

        rows = runQueryInOrders("SELECT * FROM orders WHERE id = " + vars.get("orderId"));
        assertThat(rows).hasSize(1);
        assertThat(rows.getFirst().get("CUSTOMER_ID")).isEqualTo(vars.get("customer"));

        rows = runQueryInOrders("SELECT * FROM order_items WHERE order_id = " + vars.get("orderId"));
        assertThat(rows).hasSize(1);
        assertThat(rows.getFirst().get("SKU")).isEqualTo(vars.get("sku"));

        rows = runQueryInOrders("SELECT * FROM payments.PAYMENT WHERE orderId = " + vars.get("orderId"));
        assertThat(rows).hasSize(1);

    }

    @Test
    @Order(2)
    void shouldStartProcessFailAndRollback() throws SQLException {

        final var now = Instant.now();

        int ordersCount = countInOrders("orders", null);
        int orderItemsCount = countInOrders("order_items", null);
        int paymentsCount = countInOrders("payments.PAYMENT", "registration > '%s'".formatted(now.toString()));

        assertThatThrownBy(() ->
                processRunnerService.startOrderProcess(
                        "order-kubling",
                        Map.of(
                                "mustFailTechnically", Boolean.TRUE,
                                "customer", 123,
                                "sku", "SKU-006",
                                "qty", 8,
                                "unitPrice", BigDecimal.TEN
                        )
                )
        ).isInstanceOf(PvmException.class);

        int afterOrdersCount = countInOrders("orders", null);
        int afterOrderItemsCount = countInOrders("order_items", null);
        int afterPaymentsCount = countInOrders("payments.PAYMENT", "registration > '%s'".formatted(now.toString()));

        assertThat(afterOrdersCount).isEqualTo(ordersCount);
        assertThat(orderItemsCount).isEqualTo(afterOrderItemsCount);
        assertThat(afterPaymentsCount).isEqualTo(paymentsCount);

    }

}
