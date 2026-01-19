package com.kubling.samples.operaton.orders.repo;

import com.kubling.samples.operaton.orders.model.entity.Payment;

public interface PaymentRepositoryCustom {
    Payment insert(Payment payment);
    Payment update(Payment payment);
}
