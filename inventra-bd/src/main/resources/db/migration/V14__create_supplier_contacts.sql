CREATE TABLE catalog.supplier_contacts
(
    id              UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    supplier_id     UUID         NOT NULL
        REFERENCES catalog.suppliers (id) ON DELETE CASCADE,
    name            VARCHAR(150) NOT NULL,
    email           VARCHAR(150),
    phone           VARCHAR(50),
    position        VARCHAR(100),
    primary_contact BOOLEAN      NOT NULL DEFAULT FALSE,
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);