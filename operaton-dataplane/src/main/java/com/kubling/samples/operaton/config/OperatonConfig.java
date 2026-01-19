package com.kubling.samples.operaton.config;

import com.kubling.samples.operaton.tx.KublingTransactionManager;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.util.IsolationLevel;
import lombok.extern.slf4j.Slf4j;
import org.operaton.bpm.engine.ProcessEngineConfiguration;
import org.operaton.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.operaton.bpm.engine.impl.cfg.SpringBeanFactoryProxyMap;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Objects;
import java.util.UUID;

@Configuration
@Slf4j
public class OperatonConfig {

    @Bean
    @Primary
    public static DataSource createHikariDataSource() {

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(String.format("jdbc:teiid:%s@mm://%s:%s",
                System.getProperties().getOrDefault("KUBLING_VDB_NAME", "OperatonVDB"),
                System.getProperties().getOrDefault("KUBLING_HOST", "localhost"),
                System.getProperties().getOrDefault("KUBLING_PORT", "35482")));
        hikariConfig.setUsername((String) System.getProperties().getOrDefault("KUBLING_USERNAME", "sa"));
        hikariConfig.setPassword((String) System.getProperties().getOrDefault("KUBLING_PASSWORD", "sa"));
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

    @Bean
    @Primary
    public ProcessEngineConfigurationImpl processEngineConfiguration(
            DataSource dataSource,
            PlatformTransactionManager transactionManager,
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
    @Primary
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new KublingTransactionManager(dataSource);
    }

}
