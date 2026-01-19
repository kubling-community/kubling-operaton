package com.kubling.samples.operaton.orders.delegate;

import com.kubling.samples.operaton.orders.model.entity.Payment;
import com.kubling.samples.operaton.orders.repo.PaymentRepository;
import com.kubling.samples.operaton.orders.repo.PaymentRepositoryCustom;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.operaton.bpm.engine.delegate.DelegateExecution;
import org.operaton.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Component("processPaymentDelegate")
@Transactional
public class ProcessPaymentDelegate implements JavaDelegate {

    private final PaymentRepositoryCustom paymentRepository;

    public ProcessPaymentDelegate(PaymentRepositoryCustom paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public void execute(DelegateExecution execution) {

        Long orderId = (Long) execution.getVariable("orderId");
        BigDecimal amount = (BigDecimal) execution.getVariableTyped("totalAmount").getValue();
        log.debug(amount.toPlainString());

        paymentRepository.insert(Payment.builder()
                .orderId(orderId)
                .amount(amount)
                .currency("EUR")
                .status("PENDING")
                .build());

    }
}
