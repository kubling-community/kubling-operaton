package com.kubling.samples.operaton.orders.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table("orders.ORDER_ITEMS")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItems implements Persistable<Long> {

    @Id
    @Column("ID")
    Long id;

    @Column("ORDER_ID")
    Long orderId;

    @Column("SKU")
    String sku;

    @Column("QUANTITY")
    Integer quantity;

    @Column("UNIT_PRICE")
    BigDecimal unitPrice;

    @Transient
    private boolean newRecord = false;

    @Override
    @Transient
    public boolean isNew() {
        return newRecord;
    }

}
