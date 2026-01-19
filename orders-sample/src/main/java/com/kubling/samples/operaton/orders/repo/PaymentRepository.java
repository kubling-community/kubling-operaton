package com.kubling.samples.operaton.orders.repo;

import com.kubling.samples.operaton.orders.model.entity.Payment;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends CrudRepository<Payment, Long> {
}
