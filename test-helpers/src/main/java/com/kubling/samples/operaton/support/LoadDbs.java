package com.kubling.samples.operaton.support;

import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.lifecycle.Startables;

import java.time.Duration;
import java.util.stream.Stream;

@Slf4j
public class LoadDbs extends AbstractContainersConfig {

    public static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withNetwork(network)
            .withNetworkAliases("mysql")
            .withDatabaseName("operaton_tx")
            .withUsername("root")
            .withPassword("test")
            .withInitScript("runtime-mysql.sql")
            .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(2)));

    public static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withNetwork(network)
            .withNetworkAliases("postgres")
            .withUsername("postgres")
            .withPassword("test")
            .withDatabaseName("operaton_history")
            .withInitScript("history-postgres.sql")
            .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(2)));


    public static void main(String[] args) throws InterruptedException {

        log.info("Starting MySQL and PostgreSQL containers...");

        Runtime.getRuntime().addShutdownHook(new Thread(LoadDbs::shutdown));

        Startables.deepStart(Stream.of(mysql, postgres)).join();

        log.info("Containers started: mysql={} postgres={}",
                mysql.getJdbcUrl(), postgres.getJdbcUrl());

        Thread.sleep(Long.MAX_VALUE);
    }


    private static void shutdown() {
        log.info("Shutting down containers gracefully...");

        try {
            if (mysql != null && mysql.isRunning()) {
                log.info("Stopping MySQL container...");
                mysql.stop();
            }
        } catch (Exception e) {
            log.warn("Error stopping MySQL container", e);
        }

        try {
            if (postgres != null && postgres.isRunning()) {
                log.info("Stopping PostgreSQL container...");
                postgres.stop();
            }
        } catch (Exception e) {
            log.warn("Error stopping PostgreSQL container", e);
        }

        log.info("All containers stopped.");
    }


}
