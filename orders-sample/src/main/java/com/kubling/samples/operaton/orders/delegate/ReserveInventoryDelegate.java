package com.kubling.samples.operaton.orders.delegate;

import com.kubling.samples.operaton.orders.repo.InventoryRepository;
import org.operaton.bpm.engine.delegate.DelegateExecution;
import org.operaton.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("reserveInventoryDelegate")
@Transactional
public class ReserveInventoryDelegate implements JavaDelegate {

    private final InventoryRepository inventoryRepository;

    public ReserveInventoryDelegate(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public void execute(DelegateExecution execution) {

        String sku = (String) execution.getVariable("sku");
        Integer qty = (Integer) execution.getVariable("qty");

        inventoryRepository.reserve(sku, qty);
    }
}
