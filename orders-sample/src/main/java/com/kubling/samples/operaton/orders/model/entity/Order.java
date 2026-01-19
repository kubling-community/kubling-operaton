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
import java.sql.Timestamp;

@Table("orders.ORDERS")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Order implements Persistable<Long> {

    @Id
    @Column("ID")
    private Long id;

    @Column("CUSTOMER_ID")
    private Integer customerId;

    @Column("TOTAL_AMOUNT")
    private BigDecimal totalAmount;

    @Column("CURRENCY")
    private String currency;

    @Column("STATUS")
    private String status;

    @Column("CREATED_AT")
    private Timestamp createdAt;

    @Column("UPDATED_AT")
    private Timestamp updatedAt;

    @Transient
    private boolean newRecord = false;

    @Override
    @Transient
    public boolean isNew() {
        return newRecord;
    }

}
