CREATE TABLE catalog.units_of_measure
(
    id         UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    code       VARCHAR(20)  NOT NULL UNIQUE,
    name       VARCHAR(100) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE catalog.suppliers
(
    id           UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    name         VARCHAR(150) NOT NULL,
    contact_name VARCHAR(150),
    phone        VARCHAR(50),
    email        VARCHAR(150),
    active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE catalog.product_categories
(
    id          UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    name        VARCHAR(120) NOT NULL UNIQUE,
    description TEXT,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE catalog.products
(
    id                 UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    sku                VARCHAR(80)    NOT NULL UNIQUE,
    name               VARCHAR(180)   NOT NULL,
    description        TEXT,
    product_type       VARCHAR(40)    NOT NULL,
    category_id        UUID REFERENCES catalog.product_categories (id),
    unit_of_measure_id UUID           NOT NULL REFERENCES catalog.units_of_measure (id),
    supplier_id        UUID REFERENCES catalog.suppliers (id),
    min_stock          NUMERIC(18, 4) NOT NULL DEFAULT 0,
    active             BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at         TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMP,

    CONSTRAINT chk_product_type CHECK (
        product_type IN (
                'RAW_MATERIAL',
                'PACKAGING',
                'FINISHED_PRODUCT',
                'SEMI_FINISHED'
            )
        )
);