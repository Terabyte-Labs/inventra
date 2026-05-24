ALTER TABLE catalog.suppliers
    ADD COLUMN code VARCHAR(100);

UPDATE catalog.suppliers
SET code = upper(replace(name, ' ', '_'));

ALTER TABLE catalog.suppliers
    ALTER COLUMN code SET NOT NULL;

ALTER TABLE catalog.suppliers
    ADD CONSTRAINT uk_suppliers_code UNIQUE (code);