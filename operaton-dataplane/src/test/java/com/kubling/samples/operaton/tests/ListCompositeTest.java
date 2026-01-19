package com.kubling.samples.operaton.tests;

import com.kubling.samples.operaton.support.DataPlaneContainersConfig;
import com.kubling.teiid.jdbc.TeiidDriver;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class ListCompositeTest {

    private static final String VDB_NAME = "OperatonVDB";
    private static final String SCHEMA_NAME = "operaton";

    @BeforeAll
    static void setupEnvironment() {
        System.setProperty("KUBLING_PORT", String.valueOf(DataPlaneContainersConfig.getKublingPort()));
    }

    @Test
    void shouldExposeAllOperatonTablesInComposite() throws SQLException {
        TeiidDriver td = new TeiidDriver();
        Properties props = new Properties();
        props.setProperty("user", "sa");
        props.setProperty("password", "mypass");

        String jdbcUrl = String.format("jdbc:teiid:%s@mm://localhost:%s", VDB_NAME, DataPlaneContainersConfig.getKublingPort());

        try (Connection connection = td.connect(jdbcUrl, props);
             PreparedStatement pstmt = connection.prepareStatement(
                     "SELECT Name FROM SYS.Tables WHERE SchemaName = ?")) {

            pstmt.setString(1, SCHEMA_NAME);

            Set<String> actualTables = new TreeSet<>();
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    actualTables.add(rs.getString("Name").toUpperCase(Locale.ROOT));
                }
            }

            log.info("Discovered {} tables in schema '{}': {}", actualTables.size(), SCHEMA_NAME, actualTables);

            Set<String> expectedTables = new TreeSet<>(getExpectedCamundaTables());

            Set<String> missing = expectedTables.stream()
                    .filter(t -> !actualTables.contains(t))
                    .collect(Collectors.toSet());

            Set<String> unexpected = actualTables.stream()
                    .filter(t -> !expectedTables.contains(t))
                    .collect(Collectors.toSet());

            if (!missing.isEmpty() || !unexpected.isEmpty()) {
                log.warn("Missing tables: {}", missing);
                log.warn("Unexpected tables: {}", unexpected);
            }

            Assertions.assertThat(missing)
                    .as("Missing expected Camunda tables")
                    .isEmpty();

            log.info("All expected Camunda tables are present.");
        }
    }

    private static List<String> getExpectedCamundaTables() {
        return Arrays.asList(
                // Runtime
                "ACT_RU_EXECUTION", "ACT_RU_TASK", "ACT_RU_VARIABLE", "ACT_RU_JOB",
                "ACT_RU_EVENT_SUBSCR", "ACT_RU_INCIDENT", "ACT_RU_AUTHORIZATION",

                // History
                "ACT_HI_PROCINST", "ACT_HI_ACTINST", "ACT_HI_VARINST", "ACT_HI_DETAIL",
                "ACT_HI_TASKINST", "ACT_HI_COMMENT", "ACT_HI_ATTACHMENT", "ACT_HI_OP_LOG",
                "ACT_HI_INCIDENT", "ACT_HI_CASEINST", "ACT_HI_CASEACTINST",
                "ACT_HI_JOB_LOG", "ACT_HI_BATCH", "ACT_HI_DECINST",
                "ACT_HI_DEC_IN", "ACT_HI_DEC_OUT",

                // Repository
                "ACT_RE_DEPLOYMENT", "ACT_RE_PROCDEF", "ACT_RE_CASE_DEF",
                "ACT_RE_DECISION_DEF", "ACT_RE_DECISION_REQ_DEF",

                // Engine
                "ACT_GE_PROPERTY", "ACT_GE_BYTEARRAY", "ACT_GE_SCHEMA_LOG",

                // Identity
                "ACT_ID_GROUP", "ACT_ID_MEMBERSHIP", "ACT_ID_USER",
                "ACT_ID_TENANT", "ACT_ID_TENANT_MEMBER"
        );
    }
}
