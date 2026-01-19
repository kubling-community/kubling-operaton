package com.kubling.samples.operaton.support;

import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.KublingContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.time.Duration;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
public final class DataPlaneContainersConfig extends AbstractContainersConfig {

    private static final String CONFIG_DIR = "../vdb/operaton";
    private static final String APP_CONFIG = "app-config.yaml";
    private static final String BUNDLE = "kubling-operaton-descriptor.zip";
    private static final String CONTAINER_CONFIG_DIR = "app_data";
    private static final String CONTAINER_APP_CONFIG = "/" + CONTAINER_CONFIG_DIR + "/" + APP_CONFIG;
    private static final String CONTAINER_BUNDLE = "/" + CONTAINER_CONFIG_DIR + "/" + BUNDLE;
    public static final int KUBLING_HTTP_PORT = 8289;

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

    public static final KublingContainer<?> kubling =
            new KublingContainer<>()
                    .withNetwork(network)
                    .dependsOn(mysql, postgres)
                    .withHttpPort(KUBLING_HTTP_PORT)
                    .withNativePort(KublingContainer.DEFAULT_NATIVE_PORT)
                    .withEnv(Map.of(
                            "MAIN_HTTP_PORT", String.valueOf(KUBLING_HTTP_PORT),
                            "ENABLE_WEB_CONSOLE", "FALSE",
                            "SCRIPT_LOG_LEVEL", "DEBUG",
                            "APP_CONFIG", CONTAINER_APP_CONFIG,
                            "DESCRIPTOR_BUNDLE", CONTAINER_BUNDLE,
                            "MYSQL_ADDRESS", "mysql",
                            "POSTGRES_ADDRESS", "postgres"
                    ))
                    .withLogConsumer(new Slf4jLogConsumer(log).withPrefix("DATA-PLANE"))
                    .withCopyFileToContainer(
                            MountableFile.forHostPath(String.format("%s/%s/%s", USER_DIR, CONFIG_DIR, APP_CONFIG)),
                            CONTAINER_APP_CONFIG
                    )
                    .withCopyFileToContainer(
                            MountableFile.forHostPath(String.format("%s/%s/%s", USER_DIR, CONFIG_DIR, BUNDLE)),
                            CONTAINER_BUNDLE
                    )
                    .withExposedPorts(KublingContainer.DEFAULT_NATIVE_PORT, KUBLING_HTTP_PORT)
                    .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(2)));

    private static volatile boolean initialized = false;

    static {
        log.debug("{}/{}/{}", USER_DIR, CONFIG_DIR, APP_CONFIG);
        initialize();
    }

    private DataPlaneContainersConfig() {
        // prevent instantiation
    }

    private static void initialize() {

        if (initialized) {
            return;
        }

        synchronized (DataPlaneContainersConfig.class) {
            if (initialized) {
                return;
            }

            KublingBundleBuilder.generateBundle(USER_DIR + "/" + CONFIG_DIR, BUNDLE, "descriptor");

            log.info("Starting MySQL, PostgreSQL and Kubling containers...");

            Startables.deepStart(mysql, postgres).join();

            log.info("MySQL and Postgres started: mysql={} postgres={}",
                    mysql.getJdbcUrl(), postgres.getJdbcUrl());

            Startables.deepStart(kubling).join();

            log.info("Kubling started on port {}",
                    kubling.getMappedPort(KublingContainer.DEFAULT_NATIVE_PORT));

            initialized = true;
        }
    }

    public static int getKublingPort() {
        initialize();
        return kubling.getMappedPort(KublingContainer.DEFAULT_NATIVE_PORT);
    }

    public static String getMySQLJdbcUrl() {
        initialize();
        return mysql.getJdbcUrl();
    }

    public static String getPostgresJdbcUrl() {
        initialize();
        return postgres.getJdbcUrl();
    }

    public static void shutdown() {
        synchronized (DataPlaneContainersConfig.class) {
            log.info("Stopping containers...");
            Stream.of(kubling, mysql, postgres)
                    .filter(org.testcontainers.containers.ContainerState::isRunning)
                    .forEach(container -> {
                        try {
                            container.stop();
                        } catch (Exception e) {
                            log.warn("Error stopping container {}: {}", container.getDockerImageName(), e.getMessage());
                        }
                    });
            initialized = false;
        }
    }

}
