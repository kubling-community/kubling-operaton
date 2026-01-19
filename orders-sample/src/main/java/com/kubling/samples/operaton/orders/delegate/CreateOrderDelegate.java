package com.kubling.samples.operaton.orders.delegate;

import com.kubling.samples.operaton.orders.model.entity.Order;
import com.kubling.samples.operaton.orders.model.entity.OrderItems;
import com.kubling.samples.operaton.orders.repo.OrderItemsRepository;
import com.kubling.samples.operaton.orders.repo.OrderRepository;
import org.apache.commons.lang3.RandomUtils;
import org.operaton.bpm.engine.delegate.DelegateExecution;
import org.operaton.bpm.engine.delegate.JavaDelegate;
import org.operaton.bpm.engine.variable.Variables;
import org.operaton.bpm.engine.variable.value.ObjectValue;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;

@Component("createOrderDelegate")
public class CreateOrderDelegate implements JavaDelegate {

    private final OrderRepository orderRepository;
    private final OrderItemsRepository orderItemsRepository;

    public CreateOrderDelegate(
            OrderRepository orderRepository,
            OrderItemsRepository orderItemsRepository) {
        this.orderRepository = orderRepository;
        this.orderItemsRepository = orderItemsRepository;
    }

    @Override
    @Transactional
    public void execute(DelegateExecution execution) {

        final Integer customerId = (Integer) execution.getVariable("customer");
        final String sku = (String) execution.getVariable("sku");
        final Integer quantity = (Integer) execution.getVariable("qty");

        BigDecimal unitPrice = (BigDecimal) execution.getVariableTyped("unitPrice").getValue();

        String currency = (String) execution.getVariable("currency");
        if (currency == null) {
            currency = "EUR";
        }

        final BigDecimal totalAmount = unitPrice.multiply(BigDecimal.valueOf(quantity));

        Order order = orderRepository.save(
                Order.builder()
                        .newRecord(true)
                        .customerId(customerId)
                        .totalAmount(totalAmount)
                        .currency(currency)
                        .status("CREATED")
                        .createdAt(Timestamp.from(Instant.now()))
                        .build()
        );

        orderItemsRepository.save(
                OrderItems.builder()
                        .newRecord(true)
                        .orderId(order.getId())
                        .sku(sku)
                        .quantity(quantity)
                        .unitPrice(unitPrice)
                        .build()
        );

        execution.setVariable("orderId", order.getId());
        execution.setVariable("totalAmount", Variables.objectValue(totalAmount));
        execution.setVariable("orderCurrency", currency);
    }
}
