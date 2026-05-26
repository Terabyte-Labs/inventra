ALTER TABLE inventory.inventory_movements
    ADD COLUMN unit_of_measure_id UUID;

ALTER TABLE inventory.inventory_movements
    ADD COLUMN requested_quantity NUMERIC(19, 6);

ALTER TABLE inventory.inventory_movements
    ADD COLUMN requested_unit_of_measure_id UUID;

ALTER TABLE manufacturing.manufacturing_order_inputs
    ADD COLUMN planned_unit_of_measure_id UUID;

ALTER TABLE manufacturing.manufacturing_order_inputs
    ADD COLUMN actual_unit_of_measure_id UUID;

ALTER TABLE manufacturing.manufacturing_order_outputs
    ADD COLUMN unit_of_measure_id UUID;

UPDATE inventory.inventory_movements m
SET unit_of_measure_id = p.unit_of_measure_id,
    requested_quantity = m.quantity,
    requested_unit_of_measure_id = p.unit_of_measure_id
    FROM catalog.products p
WHERE p.id = m.product_id
  AND m.unit_of_measure_id IS NULL;

UPDATE manufacturing.manufacturing_order_inputs i
SET planned_unit_of_measure_id = p.unit_of_measure_id,
    actual_unit_of_measure_id = p.unit_of_measure_id
    FROM catalog.products p
WHERE p.id = i.product_id
  AND i.actual_unit_of_measure_id IS NULL;

UPDATE manufacturing.manufacturing_order_outputs o
SET unit_of_measure_id = p.unit_of_measure_id
    FROM catalog.products p
WHERE p.id = o.product_id
  AND o.unit_of_measure_id IS NULL;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM inventory.inventory_movements
        WHERE unit_of_measure_id IS NULL
           OR requested_quantity IS NULL
           OR requested_unit_of_measure_id IS NULL
    ) THEN
        RAISE EXCEPTION 'Some inventory movements do not have unit data';
END IF;

    IF EXISTS (
        SELECT 1
        FROM manufacturing.manufacturing_order_inputs
        WHERE planned_unit_of_measure_id IS NULL
           OR actual_unit_of_measure_id IS NULL
    ) THEN
        RAISE EXCEPTION 'Some manufacturing order inputs do not have unit data';
END IF;

    IF EXISTS (
        SELECT 1
        FROM manufacturing.manufacturing_order_outputs
        WHERE unit_of_measure_id IS NULL
    ) THEN
        RAISE EXCEPTION 'Some manufacturing order outputs do not have unit data';
END IF;
END $$;

ALTER TABLE inventory.inventory_movements
    ALTER COLUMN unit_of_measure_id SET NOT NULL;

ALTER TABLE inventory.inventory_movements
    ALTER COLUMN requested_quantity SET NOT NULL;

ALTER TABLE inventory.inventory_movements
    ALTER COLUMN requested_unit_of_measure_id SET NOT NULL;

ALTER TABLE manufacturing.manufacturing_order_inputs
    ALTER COLUMN planned_unit_of_measure_id SET NOT NULL;

ALTER TABLE manufacturing.manufacturing_order_inputs
    ALTER COLUMN actual_unit_of_measure_id SET NOT NULL;

ALTER TABLE manufacturing.manufacturing_order_outputs
    ALTER COLUMN unit_of_measure_id SET NOT NULL;

ALTER TABLE inventory.inventory_movements
    ADD CONSTRAINT fk_inventory_movements_unit_of_measure
        FOREIGN KEY (unit_of_measure_id)
            REFERENCES catalog.units_of_measure(id);

ALTER TABLE inventory.inventory_movements
    ADD CONSTRAINT fk_inventory_movements_requested_unit_of_measure
        FOREIGN KEY (requested_unit_of_measure_id)
            REFERENCES catalog.units_of_measure(id);

ALTER TABLE manufacturing.manufacturing_order_inputs
    ADD CONSTRAINT fk_manufacturing_order_inputs_planned_unit
        FOREIGN KEY (planned_unit_of_measure_id)
            REFERENCES catalog.units_of_measure(id);

ALTER TABLE manufacturing.manufacturing_order_inputs
    ADD CONSTRAINT fk_manufacturing_order_inputs_actual_unit
        FOREIGN KEY (actual_unit_of_measure_id)
            REFERENCES catalog.units_of_measure(id);

ALTER TABLE manufacturing.manufacturing_order_outputs
    ADD CONSTRAINT fk_manufacturing_order_outputs_unit
        FOREIGN KEY (unit_of_measure_id)
            REFERENCES catalog.units_of_measure(id);

CREATE INDEX idx_inventory_movements_unit_of_measure_id
    ON inventory.inventory_movements(unit_of_measure_id);

CREATE INDEX idx_inventory_movements_requested_unit_of_measure_id
    ON inventory.inventory_movements(requested_unit_of_measure_id);

CREATE INDEX idx_manufacturing_order_inputs_planned_unit
    ON manufacturing.manufacturing_order_inputs(planned_unit_of_measure_id);

CREATE INDEX idx_manufacturing_order_inputs_actual_unit
    ON manufacturing.manufacturing_order_inputs(actual_unit_of_measure_id);

CREATE INDEX idx_manufacturing_order_outputs_unit
    ON manufacturing.manufacturing_order_outputs(unit_of_measure_id);