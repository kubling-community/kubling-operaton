package com.kubling.samples.operaton.orders;

import com.kubling.samples.operaton.orders.service.ProcessRunnerService;
import com.kubling.samples.operaton.support.AbstractContainersConfig;
import com.kubling.samples.operaton.support.OrdersContainersConfig;
import com.kubling.teiid.jdbc.TeiidDriver;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.operaton.bpm.engine.HistoryService;
import org.operaton.bpm.engine.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ExtendWith(SystemStubsExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Execution(ExecutionMode.SAME_THREAD)
public abstract class AbstractOrdersFlowIntegrationTest {

    @Autowired
    protected RepositoryService repositoryService;

    @Autowired
    protected HistoryService historyService;

    @Autowired
    protected ProcessRunnerService processRunnerService;

    @BeforeEach
    void deployProcesses() {
        deployIfMissing(repositoryService, "order-kubling", "order-kubling");
    }

    private static AtomicBoolean initialized = new AtomicBoolean(false);

    public static void deployIfMissing(RepositoryService repo, String key, String fileName) {

        long count = repo.createProcessDefinitionQuery()
                .processDefinitionKey(key)
                .count();
        if (count == 0) {
            repo.createDeployment()
                    .name(fileName)
                    .addClasspathResource("processes/%s.bpmn".formatted(fileName))
                    .deploy();
        }

        verifyDeployment(repo, fileName);
    }

    public static void verifyDeployment(RepositoryService repo, String name) {
        long count = repo.createProcessDefinitionQuery()
                .processDefinitionName(name)
                .count();

        if (count == 0) {
            throw new IllegalStateException("Process definition '%s' not deployed.".formatted(name));
        }
    }

    protected static void populateOrdersData() throws SQLException {

        if (!initialized.get()) {
            final var inventory = new String(AbstractContainersConfig.getClasspathBytes("inventory-data.sql"));
            runCommandInOrders(IOUtils.readLines(
                            IOUtils.toInputStream(inventory, StandardCharsets.UTF_8),
                            StandardCharsets.UTF_8),
                    false
            );
            initialized.set(true);
        }

    }

    static Connection getOrdersConnection() throws SQLException {

        TeiidDriver td = new TeiidDriver();
        Properties props = new Properties();
        props.setProperty("user", "sa");
        props.setProperty("password", "sa");

        return td.connect(
                String.format("jdbc:teiid:%s@mm://localhost:%s", "Orders", OrdersContainersConfig.getKublingPort()),
                props);

    }

    public static void runCommandInOrders(List<String> commands, boolean transactional) throws SQLException {

        try (Connection connection = getOrdersConnection()) {

            connection.setAutoCommit(!transactional);

            for (final var command : commands) {
                connection.prepareStatement(command).executeUpdate();
            }

            if (transactional) connection.commit();

        }
    }

    public List<Map<String, Object>> runQueryInOrders(String sql) throws SQLException {
        try (Connection conn = getOrdersConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            List<Map<String, Object>> rows = new ArrayList<>();
            ResultSetMetaData meta = rs.getMetaData();
            int cols = meta.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= cols; i++) {
                    row.put(meta.getColumnName(i), rs.getObject(i));
                }
                rows.add(row);
            }
            return rows;
        }
    }

    public int countInOrders(String table, String condition) throws SQLException {
        try (Connection conn = getOrdersConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT COUNT(*) as c FROM %s %s".formatted(
                             table,
                             StringUtils.isEmpty(condition)
                                     ? ""
                                     : "WHERE " + condition))) {

            if (!rs.next()) {
                throw new SQLException("Query did not return results.");
            }
            return rs.getInt("c");
        }
    }
}
