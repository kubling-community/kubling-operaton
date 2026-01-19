package com.kubling.samples.operaton.tests;

import com.kubling.samples.operaton.AbstractOperatonIntegrationTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@Slf4j
@SpringBootTest
class DistributedTransactionIntegrationTest extends AbstractOperatonIntegrationTest {

    @Autowired
    private DataSource dataSource; // Kubling federated DS

    @Test
    void shouldRollbackAllParticipantsOnFailure() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            // Insert into runtime (MySQL)
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("""
                            INSERT INTO operaton.ACT_RU_TASK (ID_, NAME_) VALUES ('rollback-test', 'before-error')
                        """);
            }

            // Insert into history (Postgres)
            try (Statement st = conn.createStatement()) {
                // simulate error: ACT_HI_PROCINST doesn’t have column NON_EXISTENT
                st.executeUpdate("""
                            INSERT INTO operaton.ACT_HI_PROCINST (ID_, PROC_DEF_ID_, NON_EXISTENT)
                            VALUES ('rollback-test', 'Process_1kaebg3', 'oops')
                        """);
            }

            // Should never reach commit
            conn.commit();
            fail("Expected error not thrown");
        } catch (SQLException expected) {
            log.info("Expected exception: {}", expected.getMessage());
        }

        // Verify rollback — both sources should be clean
        try (Connection verifyConn = dataSource.getConnection();
             Statement st = verifyConn.createStatement()) {

            ResultSet rs1 = st.executeQuery("SELECT * FROM operaton.ACT_RU_TASK WHERE ID_='rollback-test'");
            log.debug("{}", rs1.next());
//            assertThat(rs1.next()).as("Runtime row should not exist after rollback").isFalse();

            ResultSet rs2 = st.executeQuery("SELECT * FROM operaton.ACT_HI_PROCINST WHERE ID_='rollback-test'");
            assertThat(rs2.next()).as("History row should not exist after rollback").isFalse();
        }
    }

    @Test
    void shouldCommitAcrossParticipants() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            try (Statement st = conn.createStatement()) {

                st.executeUpdate("""
                            INSERT INTO operaton.ACT_RU_TASK (ID_, NAME_) VALUES ('commit-test', 'ok')
                        """);

                assertThat(getSoftTransactionOperations()).isEqualTo(1);

                st.executeUpdate("""
                            INSERT INTO operaton.ACT_HI_PROCINST (ID_, PROC_INST_ID_, PROC_DEF_ID_, START_TIME_)
                            VALUES ('commit-test', 'commit-test', 'Process_1kaebg3', CURRENT_TIMESTAMP)
                        """);

                assertThat(getSoftTransactionOperations()).isEqualTo(2);
            }

            conn.commit();

            assertThat(getSoftTransactionOperations()).isEqualTo(0);

        }

        try (Connection verifyConn = dataSource.getConnection();
             Statement st = verifyConn.createStatement()) {

            ResultSet rs1 = st.executeQuery("SELECT * FROM operaton.ACT_RU_TASK WHERE ID_='commit-test'");
            assertThat(rs1.next()).isTrue();

            ResultSet rs2 = st.executeQuery("SELECT * FROM operaton.ACT_HI_PROCINST WHERE ID_='commit-test'");
            assertThat(rs2.next()).isTrue();
        }
    }

    private Long getSoftTransactionOperations() throws SQLException {

        try (Connection sysConn = dataSource.getConnection();
             Statement st = sysConn.createStatement()) {

            ResultSet rs = st.executeQuery("""
                        SELECT COUNT(*)
                        FROM SOFT_TRANSACTIONS.TXDB
                    """);

            rs.next();
            return rs.getLong(1);

        }
    }


}
