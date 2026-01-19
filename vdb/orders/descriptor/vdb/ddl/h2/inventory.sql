CREATE TABLE INVENTORY (
    sku VARCHAR(255) NOT NULL,
    quantity INT NOT NULL,
    reserved INT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP,
    PRIMARY KEY (sku)
);

COMMENT ON COLUMN INVENTORY.sku IS 'Unique product identifier (SKU)';
COMMENT ON COLUMN INVENTORY.quantity IS 'Total available quantity';
COMMENT ON COLUMN INVENTORY.reserved IS 'Units currently reserved by orders';
COMMENT ON COLUMN INVENTORY.updated_at IS 'Last update timestamp';

COMMENT ON TABLE INVENTORY IS 'Represents stock levels for each SKU.';
