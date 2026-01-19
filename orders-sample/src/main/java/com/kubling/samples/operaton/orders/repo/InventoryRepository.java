package com.kubling.samples.operaton.orders.repo;

import com.kubling.samples.operaton.orders.model.entity.Inventory;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryRepository extends CrudRepository<Inventory, String> {

    @Modifying
    @Query("UPDATE inventory SET reserved = reserved + :qty WHERE sku = :sku")
    void reserve(@Param("sku") String sku, @Param("qty") Integer qty);

    @Modifying
    @Query("UPDATE inventory SET reserved = reserved - :qty, quantity = quantity - :qty WHERE sku = :sku")
    void release(@Param("sku") String sku, @Param("qty") Integer qty);
}
