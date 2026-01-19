package com.kubling.samples.operaton.orders.repo;

import com.kubling.samples.operaton.orders.model.entity.OrderItems;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemsRepository extends CrudRepository<OrderItems, Integer> {
    @Query("SELECT * FROM order_items WHERE order_id = :orderId")
    Iterable<OrderItems> findByOrderId(@Param("orderId") Long orderId);
}
