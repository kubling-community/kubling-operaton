package com.kubling.samples.operaton.tests;

import com.kubling.samples.operaton.AbstractOperatonIntegrationTest;
import org.junit.jupiter.api.Test;
import org.operaton.bpm.engine.history.HistoricVariableInstance;
import org.operaton.bpm.engine.runtime.ProcessInstance;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class OperatonVariableTest extends AbstractOperatonIntegrationTest {

    @Test
    void shouldPersistAndReadVariablesFromHistory() {
        Map<String, Object> vars = Map.of("customer", "ABC123");
        ProcessInstance instance = runtimeService.startProcessInstanceByKey("Process_1kaebg3", vars);

        HistoricVariableInstance variable = historyService
                .createHistoricVariableInstanceQuery()
                .processInstanceId(instance.getId())
                .variableName("customer")
                .singleResult();

        assertThat(variable).isNotNull();
        assertThat(variable.getValue()).isEqualTo("ABC123");
    }
}
