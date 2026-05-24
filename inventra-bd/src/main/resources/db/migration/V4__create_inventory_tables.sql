CREATE TABLE inventory.warehouses
(
    id         UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    code       VARCHAR(50)  NOT NULL UNIQUE,
    name       VARCHAR(150) NOT NULL,
    location   TEXT,
    active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE inventory.stock_balances
(
    id           UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    product_id   UUID           NOT NULL REFERENCES catalog.products (id),
    warehouse_id UUID           NOT NULL REFERENCES inventory.warehouses (id),
    quantity     NUMERIC(18, 4) NOT NULL DEFAULT 0,
    created_at   TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP,
    CONSTRAINT uq_stock_product_warehouse
        UNIQUE (product_id, warehouse_id),
    CONSTRAINT chk_stock_quantity
        CHECK (quantity >= 0)
);

CREATE TABLE inventory.inventory_movements
(
    id              UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    product_id      UUID           NOT NULL REFERENCES catalog.products (id),
    warehouse_id    UUID           NOT NULL REFERENCES inventory.warehouses (id),
    movement_type   VARCHAR(40)    NOT NULL,
    quantity        NUMERIC(18, 4) NOT NULL,
    before_quantity NUMERIC(18, 4) NOT NULL,
    after_quantity  NUMERIC(18, 4) NOT NULL,
    reference_type  VARCHAR(60),
    reference_id    UUID,
    notes           TEXT,
    created_by      UUID REFERENCES auth.users (id),
    created_at      TIMESTAMP      NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_inventory_movement_type CHECK (
        movement_type IN (
                'STOCK_IN',
                'STOCK_OUT',
                'ADJUSTMENT_IN',
                'ADJUSTMENT_OUT',
                'PRODUCTION_CONSUMPTION',
                'PRODUCTION_OUTPUT'
            )
        ),

    CONSTRAINT chk_inventory_quantity
        CHECK (quantity > 0)
);