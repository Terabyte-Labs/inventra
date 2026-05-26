ALTER TABLE manufacturing.formulas
    ADD COLUMN output_unit_of_measure_id UUID;

ALTER TABLE manufacturing.formula_items
    ADD COLUMN unit_of_measure_id UUID;

UPDATE manufacturing.formulas f
SET output_unit_of_measure_id = p.unit_of_measure_id
    FROM catalog.products p
WHERE p.id = f.product_id
  AND f.output_unit_of_measure_id IS NULL;

UPDATE manufacturing.formula_items fi
SET unit_of_measure_id = p.unit_of_measure_id
    FROM catalog.products p
WHERE p.id = fi.product_id
  AND fi.unit_of_measure_id IS NULL;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM manufacturing.formulas
        WHERE output_unit_of_measure_id IS NULL
    ) THEN
        RAISE EXCEPTION 'Some formulas do not have output_unit_of_measure_id';
END IF;

    IF EXISTS (
        SELECT 1
        FROM manufacturing.formula_items
        WHERE unit_of_measure_id IS NULL
    ) THEN
        RAISE EXCEPTION 'Some formula items do not have unit_of_measure_id';
END IF;
END $$;

ALTER TABLE manufacturing.formulas
    ALTER COLUMN output_unit_of_measure_id SET NOT NULL;

ALTER TABLE manufacturing.formula_items
    ALTER COLUMN unit_of_measure_id SET NOT NULL;

ALTER TABLE manufacturing.formulas
    ADD CONSTRAINT fk_formulas_output_unit_of_measure
        FOREIGN KEY (output_unit_of_measure_id)
            REFERENCES catalog.units_of_measure(id);

ALTER TABLE manufacturing.formula_items
    ADD CONSTRAINT fk_formula_items_unit_of_measure
        FOREIGN KEY (unit_of_measure_id)
            REFERENCES catalog.units_of_measure(id);

CREATE INDEX idx_formulas_output_unit_of_measure_id
    ON manufacturing.formulas(output_unit_of_measure_id);

CREATE INDEX idx_formula_items_unit_of_measure_id
    ON manufacturing.formula_items(unit_of_measure_id);