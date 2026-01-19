package com.kubling.samples.operaton.orders.delegate;

import com.kubling.samples.operaton.orders.model.entity.OrderIssue;
import com.kubling.samples.operaton.orders.repo.OrderIssueRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.operaton.bpm.engine.delegate.DelegateExecution;
import org.operaton.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;

@Slf4j
@Component("reportFailureDelegate")
public class ReportFailureDelegate implements JavaDelegate {

    private final OrderIssueRepository issueRepository;

    public ReportFailureDelegate(OrderIssueRepository issueRepository) {
        this.issueRepository = issueRepository;
    }

    @Override
    public void execute(DelegateExecution execution) {

        String errorCode = (String) execution.getVariable("errorCode");
        String errorMessage = (String) execution.getVariable("errorMessage");

        Long orderId = (Long) execution.getVariable("orderId");

        log.debug(errorCode);
        issueRepository.save(OrderIssue.builder()
                .newRecord(true)
                .id(RandomUtils.secure().randomInt())
                .orderId(orderId)
                .errorCode(errorCode != null ? errorCode : "UNKNOWN")
                .errorMessage(errorMessage != null ? errorMessage : "Unknown error")
                .severity("HIGH")
                .timestamp(Timestamp.from(Instant.now()))
                .build());
    }
}
