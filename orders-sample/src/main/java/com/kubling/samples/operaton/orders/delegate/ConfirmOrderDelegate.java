package com.kubling.samples.operaton.orders.delegate;

import com.kubling.samples.operaton.orders.repo.InventoryRepository;
import com.kubling.samples.operaton.orders.repo.OrderRepository;
import org.operaton.bpm.engine.delegate.DelegateExecution;
import org.operaton.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("confirmOrderDelegate")
public class ConfirmOrderDelegate implements JavaDelegate {

    private final OrderRepository orderRepository;
    private final InventoryRepository inventoryRepository;

    public ConfirmOrderDelegate(OrderRepository orderRepository, InventoryRepository inventoryRepository) {
        this.orderRepository = orderRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    @Transactional
    public void execute(DelegateExecution execution) {

        String sku = (String) execution.getVariable("sku");
        Integer qty = (Integer) execution.getVariable("qty");
        Long order = (Long) execution.getVariable("orderId");

        inventoryRepository.release(sku, qty);
        orderRepository.confirm(order);

    }

}
