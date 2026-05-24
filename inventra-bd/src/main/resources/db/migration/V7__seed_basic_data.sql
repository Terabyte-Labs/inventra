INSERT INTO auth.roles (name, description)
VALUES ('ADMIN', 'System administrator'),
       ('WAREHOUSE_OPERATOR', 'Warehouse operator'),
       ('PRODUCTION_OPERATOR', 'Production operator');

INSERT INTO catalog.units_of_measure (code, name)
VALUES ('KG', 'Kilogram'),
       ('G', 'Gram'),
       ('L', 'Liter'),
       ('ML', 'Milliliter'),
       ('PCS', 'Pieces');

INSERT INTO inventory.warehouses (code, name, location)
VALUES ('MAIN', 'Main Warehouse', 'Headquarters');