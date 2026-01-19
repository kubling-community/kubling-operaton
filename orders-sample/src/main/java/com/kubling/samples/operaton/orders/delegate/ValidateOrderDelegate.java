package com.kubling.samples.operaton.orders.delegate;

import org.operaton.bpm.engine.delegate.DelegateExecution;
import org.operaton.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("validateOrderDelegate")
public class ValidateOrderDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        Integer customerId = (Integer) execution.getVariable("customer");
        String sku = (String) execution.getVariable("sku");
        Integer qty = (Integer) execution.getVariable("qty");

        if (customerId == null || sku == null || qty == null || qty <= 0) {
            throw new IllegalArgumentException("Invalid order input");
        }
    }
}
