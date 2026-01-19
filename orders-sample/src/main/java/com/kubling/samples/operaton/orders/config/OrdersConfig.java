package com.kubling.samples.operaton.orders.config;

import com.kubling.samples.operaton.support.OrdersContainersConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.util.IsolationLevel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.core.dialect.JdbcDialect;
import org.springframework.data.jdbc.core.dialect.JdbcH2Dialect;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;

@Configuration
@Slf4j
public class OrdersConfig {

    @Bean(name = "ordersDataSource")
    public static DataSource createOrdersHikariDataSource() {

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(String.format("jdbc:teiid:%s@mm://%s:%s",
                System.getProperties().getOrDefault("KUBLING_ORDERS_VDB_NAME", "Orders"),
                System.getProperties().getOrDefault("KUBLING_ORDERS_HOST", "localhost"),
                System.getProperties().getOrDefault("KUBLING_ORDERS_PORT",
                        String.valueOf(OrdersContainersConfig.KUBLING_NATIVE_PORT))));
        hikariConfig.setUsername((String) System.getProperties().getOrDefault("KUBLING_ORDERS_USERNAME", "sa"));
        hikariConfig.setPassword((String) System.getProperties().getOrDefault("KUBLING_ORDERS_PASSWORD", "sa"));
        hikariConfig.setDriverClassName("com.kubling.teiid.jdbc.TeiidDriver");

        // Additional HikariCP settings
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setMinimumIdle(5);
        hikariConfig.setIdleTimeout(30000);
        hikariConfig.setConnectionTimeout(20000);
        hikariConfig.setAutoCommit(false);
        hikariConfig.setTransactionIsolation(IsolationLevel.TRANSACTION_READ_COMMITTED.name());

        return new HikariDataSource(hikariConfig);
    }

    @Bean(name = {"transactionManager", "ordersTransactionManager"})
    public PlatformTransactionManager ordersTransactionManager(
            @Qualifier("ordersDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource) {
            @SneakyThrows
            @Override
            protected void doBegin(Object tx, TransactionDefinition def) {
                ConnectionHolder holder = (ConnectionHolder)
                        TransactionSynchronizationManager.getResource(dataSource);

                log.debug("BEFORE super.doBegin(): holder={}, conn={}", holder,
                        (holder != null ? holder.getConnectionHandle() : null));

                super.doBegin(tx, def);

                holder = (ConnectionHolder)
                        TransactionSynchronizationManager.getResource(dataSource);

                log.debug("AFTER super.doBegin(): holder={}, conn={}, autocommit={}",
                        holder,
                        (holder != null ? holder.getConnectionHandle() : null),
                        (holder != null ?
                                holder.getConnectionHandle().getConnection().getAutoCommit() : null));
            }

            @Override
            protected void doCommit(DefaultTransactionStatus status) {
                log.debug("OrdersTM COMMIT");
                super.doCommit(status);
            }

            @Override
            protected void doRollback(DefaultTransactionStatus status) {
                log.debug("OrdersTM ROLLBACK");
                super.doRollback(status);
            }
        };

    }

    @Bean(name = "ordersJdbcTemplate")
    public NamedParameterJdbcOperations ordersJdbcTemplate(
            @Qualifier("ordersDataSource") DataSource ds) {
        return new NamedParameterJdbcTemplate(ds);
    }

    @Bean
    public JdbcDialect jdbcDialect() {
        return JdbcH2Dialect.INSTANCE;
    }
}
