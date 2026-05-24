CREATE TABLE manufacturing.formulas
(
    id              UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    code            VARCHAR(80)    NOT NULL UNIQUE,
    name            VARCHAR(180)   NOT NULL,
    product_id      UUID           NOT NULL REFERENCES catalog.products (id),
    output_quantity NUMERIC(18, 4) NOT NULL DEFAULT 1,
    version         INTEGER        NOT NULL DEFAULT 1,
    active          BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP      NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_formula_output_quantity
        CHECK (output_quantity > 0)
);

CREATE TABLE manufacturing.formula_items
(
    id         UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    formula_id UUID           NOT NULL
        REFERENCES manufacturing.formulas (id) ON DELETE CASCADE,
    product_id UUID           NOT NULL REFERENCES catalog.products (id),
    quantity   NUMERIC(18, 4) NOT NULL,
    line_order INTEGER        NOT NULL DEFAULT 1,
    notes      TEXT,
    CONSTRAINT chk_formula_item_quantity
        CHECK (quantity > 0)
);

CREATE TABLE manufacturing.manufacturing_orders
(
    id               UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    order_number     VARCHAR(80)    NOT NULL UNIQUE,
    formula_id       UUID           NOT NULL REFERENCES manufacturing.formulas (id),
    product_id       UUID           NOT NULL REFERENCES catalog.products (id),
    planned_quantity NUMERIC(18, 4) NOT NULL,
    actual_quantity  NUMERIC(18, 4),
    warehouse_id     UUID           NOT NULL REFERENCES inventory.warehouses (id),
    status           VARCHAR(40)    NOT NULL DEFAULT 'DRAFT',
    started_at       TIMESTAMP,
    completed_at     TIMESTAMP,
    created_by       UUID REFERENCES auth.users (id),
    created_at       TIMESTAMP      NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_manufacturing_status CHECK (
        status IN (
            'DRAFT',
            'IN_PROGRESS',
            'COMPLETED',
            'CANCELLED'
        )
    )
);

CREATE TABLE manufacturing.manufacturing_order_inputs
(
    id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    manufacturing_order_id UUID           NOT NULL
        REFERENCES manufacturing.manufacturing_orders (id) ON DELETE CASCADE,
    product_id             UUID           NOT NULL REFERENCES catalog.products (id),
    planned_quantity       NUMERIC(18, 4) NOT NULL,
    actual_quantity        NUMERIC(18, 4)
);

CREATE TABLE manufacturing.manufacturing_order_outputs
(
    id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    manufacturing_order_id UUID           NOT NULL
        REFERENCES manufacturing.manufacturing_orders (id) ON DELETE CASCADE,
    product_id             UUID           NOT NULL REFERENCES catalog.products (id),
    quantity               NUMERIC(18, 4) NOT NULL
);