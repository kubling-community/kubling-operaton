package com.kubling.samples.operaton.orders.delegate;

import com.kubling.samples.operaton.orders.repo.OrderItemsRepository;
import com.kubling.samples.operaton.orders.repo.OrderRepository;
import com.kubling.samples.operaton.orders.repo.PaymentRepository;
import com.kubling.samples.operaton.orders.repo.PaymentRepositoryCustom;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.operaton.bpm.engine.delegate.BpmnError;
import org.operaton.bpm.engine.delegate.DelegateExecution;
import org.operaton.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component("sendContinuationMessageDelegate")
@Slf4j
public class SendContinuationMessageDelegate implements JavaDelegate {

    private final OrderRepository orderRepository;
    private final OrderItemsRepository orderItemsRepository;
    private final PaymentRepository paymentRepository;

    public SendContinuationMessageDelegate(
            OrderRepository orderRepository,
            OrderItemsRepository orderItemsRepository,
            PaymentRepository paymentRepository) {
        this.orderRepository = orderRepository;
        this.orderItemsRepository = orderItemsRepository;
        this.paymentRepository = paymentRepository;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {

        // We use this delegate to assert on data
        Long order = (Long) execution.getVariable("orderId");
        final var orderEntity = orderRepository.findById(order);
        if (orderEntity.isEmpty()) {
            throw new Exception("Order not registered.");
        }

        final var items = orderItemsRepository.findByOrderId(order).iterator();
        if (!items.hasNext()) {
            throw new Exception("Order items not registered.");
        }

        final var payment = paymentRepository.findById(order);
        if (payment.isEmpty()) {
            throw new Exception("Payment not registered.");
        }

        Boolean mustFailBusiness = (Boolean) execution.getVariable("mustFailBusiness");
        if (BooleanUtils.isTrue(mustFailBusiness)) {
            execution.setVariable("errorCode", "MUST_FAIL");
            execution.setVariable("errorMessage", "Marked for Business Error.");
            throw new BpmnError("Business error, rollback not explicitelly called.");
        }
        Boolean mustFailTechnically = (Boolean) execution.getVariable("mustFailTechnically");
        if (BooleanUtils.isTrue(mustFailTechnically)) {
            throw new Exception("Technical Error -> Forcing a process rollback.");
        }

        log.debug("Sending message to notifiers...");
    }
}
