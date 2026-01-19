package com.kubling.samples.operaton.tests;

import com.kubling.samples.operaton.AbstractOperatonIntegrationTest;
import org.junit.jupiter.api.Test;
import org.operaton.bpm.engine.history.HistoricProcessInstance;
import org.operaton.bpm.engine.runtime.ProcessInstance;

import static org.assertj.core.api.Assertions.assertThat;

public class OperatonRuntimeTest extends AbstractOperatonIntegrationTest {

    @Test
    void shouldStartProcessAndWriteHistory() {
        ProcessInstance instance = runtimeService.startProcessInstanceByKey("Process_1kaebg3");
        assertThat(instance).isNotNull();

        HistoricProcessInstance history = historyService
                .createHistoricProcessInstanceQuery()
                .processInstanceId(instance.getId())
                .singleResult();

        assertThat(history).isNotNull();
        assertThat(history.getProcessDefinitionKey()).isEqualTo("Process_1kaebg3");
    }
}
