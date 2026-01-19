package com.kubling.samples.operaton.orders.service;

import org.operaton.bpm.engine.RuntimeService;
import org.operaton.bpm.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class ProcessRunnerService {

    private final RuntimeService runtimeService;

    public ProcessRunnerService(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    @Transactional("ordersTransactionManager")
    public ProcessInstance startOrderProcess(String processDefinitionKey, Map<String, Object> vars) {
        return runtimeService.startProcessInstanceByKey(processDefinitionKey, vars);
    }

}
