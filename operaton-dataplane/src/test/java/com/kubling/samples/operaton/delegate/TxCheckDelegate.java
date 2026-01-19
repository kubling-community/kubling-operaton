package com.kubling.samples.operaton.delegate;

import lombok.extern.slf4j.Slf4j;
import org.operaton.bpm.engine.delegate.DelegateExecution;
import org.operaton.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
@Slf4j
public class TxCheckDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        boolean isActive = TransactionSynchronizationManager.isActualTransactionActive();
        log.info("Spring TX active: {}", isActive);
    }
}
