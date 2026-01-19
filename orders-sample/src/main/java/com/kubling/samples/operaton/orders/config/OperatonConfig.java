package com.kubling.samples.operaton.orders.config;

import com.kubling.samples.operaton.orders.tx.KublingTransactionManager;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.util.IsolationLevel;
import lombok.extern.slf4j.Slf4j;
import org.operaton.bpm.engine.ProcessEngineConfiguration;
import org.operaton.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.operaton.bpm.engine.impl.cfg.SpringBeanFactoryProxyMap;
import org.operaton.bpm.spring.boot.starter.configuration.OperatonDatasourceConfiguration;
import org.operaton.bpm.spring.boot.starter.configuration.impl.DefaultDatasourceConfiguration;
import org.operaton.bpm.spring.boot.starter.property.OperatonBpmProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionStatus;

import javax.sql.DataSource;
import java.util.Optional;
import java.util.UUID;

@Configuration
@Slf4j
public class OperatonConfig {

    @Bean(name = "operatonDataSource")
    public static DataSource createOperatonDataplaneHikariDataSource() {

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(String.format("jdbc:teiid:%s@mm://%s:%s",
                System.getProperties().getOrDefault("KUBLING_OPERATON_VDB_NAME", "OperatonVDB"),
                System.getProperties().getOrDefault("KUBLING_OPERATON_HOST", "localhost"),
                System.getProperties().getOrDefault("KUBLING_OPERATON_PORT", "35482")));
        hikariConfig.setUsername((String) System.getProperties().getOrDefault("KUBLING_OPERATON_USERNAME", "sa"));
        hikariConfig.setPassword((String) System.getProperties().getOrDefault("KUBLING_OPERATON_PASSWORD", "sa"));
        hikariConfig.setDriverClassName("com.kubling.teiid.jdbc.TeiidDriver");

        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setMinimumIdle(5);
        hikariConfig.setIdleTimeout(30000);
        hikariConfig.setConnectionTimeout(20000);
        hikariConfig.setAutoCommit(false);
        hikariConfig.setTransactionIsolation(IsolationLevel.TRANSACTION_READ_COMMITTED.name());

        return new HikariDataSource(hikariConfig);
    }

    @Bean(name = "operatonTransactionManager")
    @Primary
    public PlatformTransactionManager operatonTransactionManager(
            @Qualifier("operatonDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource) {
            @Override
            protected void doBegin(Object tx, TransactionDefinition def) {
                log.debug("OperatonTM BEGIN: {}", def);
                super.doBegin(tx, def);
            }

            @Override
            protected void doCommit(DefaultTransactionStatus status) {
                log.debug("OperatonTM COMMIT");
                super.doCommit(status);
            }

            @Override
            protected void doRollback(DefaultTransactionStatus status) {
                log.debug("OperatonTM ROLLBACK");
                super.doRollback(status);
            }
        };
    }

    @Bean
    public ProcessEngineConfigurationImpl processEngineConfiguration(
            @Qualifier("operatonDataSource") DataSource dataSource,
            @Qualifier("operatonTransactionManager") PlatformTransactionManager transactionManager,
            ApplicationContext applicationContext) {

        KublingProcessEngineConfiguration config = new KublingProcessEngineConfiguration();

        config.setDataSource(dataSource);
        config.setTransactionManager(transactionManager);
        config.setTransactionsExternallyManaged(true);

        // Other settings
        config.setDatabaseType("h2");
        config.setDatabaseSchema("operaton");
        config.setDatabaseTablePrefix("operaton.");
        config.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_FALSE);
        config.setHistoryTimeToLive("P365D");
        config.setIdGenerator(() -> UUID.randomUUID().toString());
        config.setMetricsEnabled(false);
        config.setJobExecutorActivate(false);
        config.setBeans(new SpringBeanFactoryProxyMap(applicationContext));

        return config;
    }

    @Bean
    public OperatonDatasourceConfiguration operatonDatasourceConfiguration(
            OperatonBpmProperties props,
            @Qualifier("operatonTransactionManager") PlatformTransactionManager tm,
            PlatformTransactionManager operatonTransactionManager,
            @Qualifier("operatonDataSource") DataSource ds,
            DataSource operatonDataSource) {

        return new DefaultDatasourceConfiguration(
                props,
                tm,
                Optional.of(operatonTransactionManager),
                ds,
                Optional.of(operatonDataSource)
        );
    }

}
