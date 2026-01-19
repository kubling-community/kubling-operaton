CREATE TABLE ORDERS (
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    customer_id INT NOT NULL,
    total_amount DECIMAL NOT NULL,
    currency VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    PRIMARY KEY (id)
);

COMMENT ON COLUMN ORDERS.id IS 'Unique order identifier';
COMMENT ON COLUMN ORDERS.customer_id IS 'Customer placing the order';
COMMENT ON COLUMN ORDERS.total_amount IS 'Total amount of the order';
COMMENT ON COLUMN ORDERS.currency IS 'Currency for the transaction';
COMMENT ON COLUMN ORDERS.status IS 'Order status (NEW, PENDING, CONFIRMED, CANCELLED)';
COMMENT ON COLUMN ORDERS.created_at IS 'Creation timestamp';
COMMENT ON COLUMN ORDERS.updated_at IS 'Last update timestamp';

-- *******************************************************************************************
COMMENT ON TABLE ORDERS IS 'Represents an order entity managed by the business database.';

CREATE TABLE ORDER_ITEMS (
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    order_id INT NOT NULL,
    sku VARCHAR(255) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL NOT NULL,
    PRIMARY KEY (id)
);

COMMENT ON COLUMN ORDER_ITEMS.id IS 'Unique item identifier';
COMMENT ON COLUMN ORDER_ITEMS.order_id IS 'Foreign key to ORDERS.id (not enforced at DB level)';
COMMENT ON COLUMN ORDER_ITEMS.sku IS 'SKU of the item';
COMMENT ON COLUMN ORDER_ITEMS.quantity IS 'Units requested in the order line';
COMMENT ON COLUMN ORDER_ITEMS.unit_price IS 'Price per unit';

COMMENT ON TABLE ORDER_ITEMS IS 'Represents a line item within an order.';
