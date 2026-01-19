package com.kubling.samples.operaton.orders.tx;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;

@Slf4j
public class KublingTransactionManager extends DataSourceTransactionManager {

    public KublingTransactionManager(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) {

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            // Already in a transaction - we should join it
            log.debug("KublingTransactionManager - existing transaction found, joining instead of beginning new one");
            return;
        }

        super.doBegin(transaction, definition);
    }

    @Override
    protected void doCommit(DefaultTransactionStatus status) {

        if (!status.isNewTransaction()) {
            log.debug("KublingTransactionManager - skipping commit for existing transaction");
            return;
        }

        super.doCommit(status);
    }

    @Override
    protected void doRollback(DefaultTransactionStatus status) {
        if (!status.isNewTransaction()) {
            log.debug("KublingTransactionManager - skipping rollback for existing transaction");
            return;
        }

        super.doRollback(status);
    }
}

