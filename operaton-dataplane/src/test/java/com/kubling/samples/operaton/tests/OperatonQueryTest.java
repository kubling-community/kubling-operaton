package com.kubling.samples.operaton.tests;

import com.kubling.samples.operaton.AbstractOperatonIntegrationTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.operaton.bpm.engine.history.HistoricProcessInstance;
import org.operaton.bpm.engine.runtime.ProcessInstance;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class OperatonQueryTest extends AbstractOperatonIntegrationTest {

    @Test
    void shouldListMultipleHistoricProcesses() {
        for (int i = 0; i < 3; i++) {
            ProcessInstance instance = runtimeService.startProcessInstanceByKey("Process_1kaebg3");
            assertThat(instance.getId()).isNotNull();
        }

        List<HistoricProcessInstance> historyList = historyService
                .createHistoricProcessInstanceQuery()
                .processDefinitionKey("Process_1kaebg3")
                .finished()
                .list();

        assertThat(historyList).hasSizeGreaterThanOrEqualTo(3);

        for (final var h : historyList) {
            log.debug(h.getStartTime().toString());
            log.debug(h.getEndTime().toString());
            log.debug(h.getState());
        }
    }
}
