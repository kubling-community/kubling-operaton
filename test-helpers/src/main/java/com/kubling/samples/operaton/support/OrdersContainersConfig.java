package com.kubling.samples.operaton.support;

import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KublingContainer;
import org.testcontainers.containers.SelinuxContext;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.MountableFile;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
public final class OrdersContainersConfig extends AbstractContainersConfig {

    private static final String CONFIG_DIR = "../vdb/orders";
    private static final String MVCC_DIR = "../vdb/mvcc";
    private static final String CONTAINER_CONFIG_DIR = "app_data";
    private static final int PAYMENTS_API_PORT = 8080;
    private static final int ORDERS_ISSUES_API_PORT = 8081;
    public static final int KUBLING_HTTP_PORT = 8287;
    public static final int KUBLING_NATIVE_PORT = 35483;

    public static final GenericContainer<?> paymentsServer = new GenericContainer<>("python:3.11-alpine")
            .withExposedPorts(PAYMENTS_API_PORT)
            .withNetworkAliases("payments")
            .withNetwork(network)
            .withCopyToContainer(
                    Transferable.of(getClasspathBytes("payments-api/server.py")),
                    "/app/server.py"
            )
            .withCopyToContainer(
                    Transferable.of(getClasspathBytes("payments-api/payments.json")),
                    "/app/payments.json"
            )
            .withCommand("sh", "-c", "pip install flask && python /app/server.py")
            .waitingFor(Wait.forHttp("/payments").forStatusCode(200));

    public static final GenericContainer<?> orderIssuesServer =
            new GenericContainer<>("python:3.11-alpine")
                    .withExposedPorts(ORDERS_ISSUES_API_PORT)
                    .withNetwork(network)
                    .withNetworkAliases("issues")
                    .withCopyToContainer(
                            Transferable.of(getClasspathBytes("order-issues-api/server.py")),
                            "/app/server.py"
                    )
                    .withCommand("sh", "-c",
                            "pip install flask && python /app/server.py"
                    )
                    .waitingFor(Wait.forHttp("/issues").forStatusCode(200));

    public static final KublingContainer<?> kublingOrders =
            new KublingContainer<>()
                    .withNetwork(network)
                    .withHttpPort(KUBLING_HTTP_PORT)
                    .withNativePort(KUBLING_NATIVE_PORT)
                    .dependsOn(paymentsServer, orderIssuesServer)
                    .withEnv(envVars())
                    .withLogConsumer(new Slf4jLogConsumer(log).withPrefix("ORDERS"))
                    .withCopyFileToContainer(
                            MountableFile.forHostPath(String.format("%s/%s/%s", USER_DIR, CONFIG_DIR, "app-config.yaml")),
                            "/%s/%s".formatted(CONTAINER_CONFIG_DIR, "app-config.yaml")
                    )
                    .withCopyFileToContainer(
                            MountableFile.forHostPath(String.format("%s/%s/%s", USER_DIR, CONFIG_DIR, "orders-descriptor-bundle.zip")),
                            "/%s/%s".formatted(CONTAINER_CONFIG_DIR, "orders-descriptor-bundle.zip")
                    )
                    .withCopyFileToContainer(
                            MountableFile.forHostPath(String.format("%s/%s/%s", USER_DIR, CONFIG_DIR, "mod-apis-bundle.zip")),
                            "/%s/%s".formatted(CONTAINER_CONFIG_DIR, "mod-apis-bundle.zip")
                    )
                    .withExposedPorts(KUBLING_NATIVE_PORT, KUBLING_HTTP_PORT)
                    .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(2)));


    private static volatile boolean initialized = false;

    private OrdersContainersConfig() {
        // prevent instantiation
    }

    private static void initialize(SoftTransactionStrategy strategy) {

        if (initialized) {
            return;
        }
        synchronized (OrdersContainersConfig.class) {

            if (initialized) {
                return;
            }

            KublingBundleBuilder.generateBundle(
                    USER_DIR + "/" + CONFIG_DIR, "orders-descriptor-bundle.zip", "descriptor"
            );
            KublingBundleBuilder.generateBundle(
                    USER_DIR + "/" + CONFIG_DIR, "mod-apis-bundle.zip", "mod-apis"
            );

            log.info("Starting API Server containers...");

            Startables.deepStart(paymentsServer, orderIssuesServer).join();

            log.info("Servers started: payments={} order-issues={}",
                    paymentsServer.getMappedPort(PAYMENTS_API_PORT), orderIssuesServer.getMappedPort(ORDERS_ISSUES_API_PORT));

            if (strategy.equals(SoftTransactionStrategy.DEFER_OPERATION)) {
                kublingOrders.addEnv("STX_STRATEGY", "DEFER_OPERATION");
                // In case of operation being deferred (retained in the Kubling engine until commit) we activate the MVCC
                // so the processes can read what was written in previous tasks.
                kublingOrders.addEnv("MVCC_ENABLED", "true");

                kublingOrders.addFileSystemBind(
                        USER_DIR + "/" + MVCC_DIR,
                        "/mvcc",
                        BindMode.READ_WRITE,
                        SelinuxContext.NONE
                );

            } else {
                kublingOrders.addEnv("STX_STRATEGY", "IMMEDIATE_OPERATION");
                kublingOrders.addEnv("MVCC_ENABLED", "false");
            }

            Startables.deepStart(kublingOrders).join();

            log.info("Kubling started on port {}",
                    kublingOrders.getMappedPort(KUBLING_NATIVE_PORT));

            initialized = true;
        }

    }

    public static int getKublingPort(SoftTransactionStrategy strategy) {
        initialize(strategy);
        return kublingOrders.getMappedPort(KUBLING_NATIVE_PORT);
    }

    public static void shutdown() {
        synchronized (OrdersContainersConfig.class) {
            log.info("Stopping containers...");
            Stream.of(kublingOrders)
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

    public static int getKublingPort() {
        return kublingOrders.getMappedPort(KUBLING_NATIVE_PORT);
    }

    private static Map<String, String> envVars() {
        final var env = new HashMap<String, String>();
        env.put("MAIN_HTTP_PORT", String.valueOf(KUBLING_HTTP_PORT));
        env.put("NATIVE_PORT", String.valueOf(KUBLING_NATIVE_PORT));
        env.put("ENABLE_WEB_CONSOLE", "FALSE");
        env.put("SCRIPT_LOG_LEVEL", "DEBUG");
        env.put("APP_CONFIG", "/%s/%s".formatted(CONTAINER_CONFIG_DIR, "app-config.yaml"));
        env.put("DESCRIPTOR_BUNDLE", "/%s/%s".formatted(CONTAINER_CONFIG_DIR, "orders-descriptor-bundle.zip"));
        env.put("MODULE_BUNDLE", "/%s/%s".formatted(CONTAINER_CONFIG_DIR, "mod-apis-bundle.zip"));
        env.put("PAYMENTS_URL", "http://payments:" + PAYMENTS_API_PORT);
        env.put("ORDER_ISSUES_URL", "http://issues:" + ORDERS_ISSUES_API_PORT);
        return env;
    }

}
