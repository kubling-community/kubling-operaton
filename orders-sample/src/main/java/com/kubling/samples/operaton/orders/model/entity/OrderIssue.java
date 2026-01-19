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

import java.sql.Timestamp;

@Table("ORDER_ISSUES")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderIssue implements Persistable<Integer> {

    @Id
    @Column("id")
    private Integer id;

    @Column("orderId")
    private Long orderId;

    @Column("errorCode")
    private String errorCode;

    @Column("errorMessage")
    private String errorMessage;

    @Column("severity")
    private String severity;

    @Column("timestamp")
    private Timestamp timestamp;

    @Transient
    private boolean newRecord = false;

    @Override
    @Transient
    public boolean isNew() {
        return newRecord;
    }

}

