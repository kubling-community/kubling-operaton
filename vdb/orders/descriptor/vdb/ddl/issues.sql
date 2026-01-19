CREATE FOREIGN TABLE ORDER_ISSUES
(
    id integer NOT NULL OPTIONS(ANNOTATION 'Issue ID'),
    orderId long OPTIONS(ANNOTATION 'Associated Order'),
    errorCode string,
    errorMessage string,
    severity string,
    "timestamp" timestamp,

    PRIMARY KEY(id)
)
OPTIONS(
    updatable true,
    supports_idempotency true,
    tags 'issues;ops;incident',
    ANNOTATION 'Represents business-level issues for failed orders'
);