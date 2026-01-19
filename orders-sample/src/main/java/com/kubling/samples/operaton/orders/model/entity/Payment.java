package com.kubling.samples.operaton.orders.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Table("payments.PAYMENT")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Payment implements Persistable<Long> {

    @Id
    @Column("orderId")
    Long orderId;

    @Column("amount")
    BigDecimal amount;

    @Column("currency")
    String currency;

    @Column("status")
    String status;

    @Column("externalTxId")
    String externalTxId;

    @Column("registration")
    Timestamp registration;

    @Transient
    private boolean newRecord = false;

    @Override
    @Transient
    public boolean isNew() {
        return newRecord;
    }

    @Override
    public Long getId() {
        return this.orderId;
    }
    
}
