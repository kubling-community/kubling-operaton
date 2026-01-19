package com.kubling.samples.operaton.orders.repo;

import com.kubling.samples.operaton.orders.model.entity.Order;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends CrudRepository<Order, Long> {
    @Modifying
    @Query("UPDATE orders SET status = 'PROCESSED' WHERE id = :orderId")
    void confirm(@Param("orderId") Long orderId);
}
