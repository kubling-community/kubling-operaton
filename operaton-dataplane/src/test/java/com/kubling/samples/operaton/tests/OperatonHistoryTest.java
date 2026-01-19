package com.kubling.samples.operaton.tests;

import com.kubling.samples.operaton.AbstractOperatonIntegrationTest;
import org.junit.jupiter.api.Test;
import org.operaton.bpm.engine.history.HistoricProcessInstance;
import org.operaton.bpm.engine.runtime.ProcessInstance;

import static org.assertj.core.api.Assertions.assertThat;

public class OperatonHistoryTest extends AbstractOperatonIntegrationTest {

    @Test
    void shouldRecordFinishedProcess() {
        ProcessInstance instance = runtimeService.startProcessInstanceByKey("Process_with_wait");
        runtimeService.deleteProcessInstance(instance.getId(), "test-complete");

        HistoricProcessInstance history = historyService
                .createHistoricProcessInstanceQuery()
                .processInstanceId(instance.getId())
                .finished()
                .singleResult();

        assertThat(history).isNotNull();
        assertThat(history.getDeleteReason()).isEqualTo("test-complete");
    }
}
