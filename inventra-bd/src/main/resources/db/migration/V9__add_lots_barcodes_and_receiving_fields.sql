CREATE TABLE catalog.product_barcodes
(
    id         UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    product_id UUID         NOT NULL REFERENCES catalog.products (id),
    barcode    VARCHAR(120) NOT NULL UNIQUE,
    active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE inventory.product_lots
(
    id                    UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    product_id            UUID         NOT NULL REFERENCES catalog.products (id),
    supplier_id           UUID REFERENCES catalog.suppliers (id),
    lot_number            VARCHAR(120) NOT NULL,
    serial_number         VARCHAR(120),
    presentation_quantity NUMERIC(18, 4),
    presentation_unit     VARCHAR(20),
    unit_price            NUMERIC(18, 4),
    received_at           TIMESTAMP    NOT NULL DEFAULT NOW(),
    created_at            TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_product_lot UNIQUE (product_id, lot_number)
);

ALTER TABLE inventory.inventory_movements
    ADD COLUMN product_lot_id UUID REFERENCES inventory.product_lots (id),
    ADD COLUMN unit_price NUMERIC(18, 4),
    ADD COLUMN storage_location VARCHAR(120);