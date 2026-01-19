package com.kubling.samples.operaton.orders.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.sql.Timestamp;

@Table("inventory.INVENTORY")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Inventory {

    @Id
    @Column("SKU")
    String sku;

    @Column("QUANTITY")
    Integer quantity;

    @Column("RESERVED")
    Integer reserved;

    @Column("UPDATED_AT")
    Timestamp updatedAt;

}
