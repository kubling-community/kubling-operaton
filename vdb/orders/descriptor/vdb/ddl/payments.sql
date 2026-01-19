CREATE FOREIGN TABLE PAYMENT
(
    apiURL string OPTIONS(
        val_constant '{{ PAYMENTS_URL }}',
        ANNOTATION 'URL of the API service'
    ),

    orderId long NOT NULL OPTIONS(
        ANNOTATION 'Order identifier associated with this payment'
    ),

    amount bigdecimal OPTIONS(
        ANNOTATION 'Amount requested for processing'
    ),

    currency string OPTIONS(
        ANNOTATION 'Currency used for payment'
    ),

    status string OPTIONS(
        ANNOTATION 'Current payment status (OK, FAILED, PENDING)'
    ),

    externalTxId string OPTIONS(
        ANNOTATION 'External transaction ID returned by provider'
    ),

    registration timestamp OPTIONS(
        name_in_source 'timestamp',
        ANNOTATION 'Payment registration timestamp'
    ),

    PRIMARY KEY(orderId),
    UNIQUE(externalTxId),
    ACCESSPATTERN(orderId), ACCESSPATTERN(status), ACCESSPATTERN(registration)
)
OPTIONS(
    updatable true,
    supports_idempotency true,
    tags 'payment;external_api;orders',
    ANNOTATION 'Represents a payment document backed by the Payment API.',
    relationship_affects 'ORDERS',
    relationship_affected_by 'ORDER_ITEMS'
);
