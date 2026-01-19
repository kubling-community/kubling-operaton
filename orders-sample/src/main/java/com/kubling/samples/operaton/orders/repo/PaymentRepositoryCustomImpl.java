package com.kubling.samples.operaton.orders.repo;

import com.kubling.samples.operaton.orders.model.entity.Payment;
import lombok.Getter;
import org.springframework.data.jdbc.core.convert.DataAccessStrategy;
import org.springframework.data.jdbc.core.convert.Identifier;
import org.springframework.data.jdbc.core.convert.InsertSubject;
import org.springframework.data.relational.core.conversion.IdValueSource;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PaymentRepositoryCustomImpl implements PaymentRepositoryCustom {

    private final DataAccessStrategy das;
    private final RelationalMappingContext context;
    @Getter
    private final PaymentRepository paymentRepository;

    public PaymentRepositoryCustomImpl(
            DataAccessStrategy das,
            RelationalMappingContext context,
            PaymentRepository repository) {
        this.das = das;
        this.context = context;
        this.paymentRepository = repository;
    }

    @Override
    public Payment insert(Payment p) {

        RelationalPersistentEntity<?> entity =
                context.getRequiredPersistentEntity(Payment.class);

        RelationalPersistentProperty idProperty =
                entity.getRequiredIdProperty();

        SqlIdentifier columnName = idProperty.getColumnName();

        Identifier identifier = Identifier.of(
                columnName,
                p.getOrderId(),
                idProperty.getType()
        );

        InsertSubject<Payment> subject =
                InsertSubject.describedBy(p, identifier);

        das.insert(
                List.of(subject),
                Payment.class,
                IdValueSource.PROVIDED
        );

        return p;
    }

    @Override
    public Payment update(Payment p) {
        das.update(p, Payment.class);
        return p;
    }

}
