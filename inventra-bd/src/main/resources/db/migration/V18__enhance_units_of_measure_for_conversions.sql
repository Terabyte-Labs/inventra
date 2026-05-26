ALTER TABLE catalog.units_of_measure
    ADD COLUMN unit_group VARCHAR(30);

ALTER TABLE catalog.units_of_measure
    ADD COLUMN conversion_factor_to_base NUMERIC(19, 6);

UPDATE catalog.units_of_measure
SET unit_group = 'WEIGHT',
    conversion_factor_to_base = 1.000000
WHERE code = 'G';

UPDATE catalog.units_of_measure
SET unit_group = 'WEIGHT',
    conversion_factor_to_base = 1000.000000
WHERE code = 'KG';

UPDATE catalog.units_of_measure
SET unit_group = 'VOLUME',
    conversion_factor_to_base = 1.000000
WHERE code = 'ML';

UPDATE catalog.units_of_measure
SET unit_group = 'VOLUME',
    conversion_factor_to_base = 1000.000000
WHERE code = 'L';

UPDATE catalog.units_of_measure
SET unit_group = 'COUNT',
    conversion_factor_to_base = 1.000000
WHERE code IN ('PCS', 'PZA', 'PIECE');

INSERT INTO catalog.units_of_measure (
    id,
    code,
    name,
    unit_group,
    conversion_factor_to_base,
    created_at
)
SELECT gen_random_uuid(), 'G', 'Gramo', 'WEIGHT', 1.000000, NOW()
    WHERE NOT EXISTS (
    SELECT 1 FROM catalog.units_of_measure WHERE code = 'G'
);

INSERT INTO catalog.units_of_measure (
    id,
    code,
    name,
    unit_group,
    conversion_factor_to_base,
    created_at
)
SELECT gen_random_uuid(), 'KG', 'Kilogramo', 'WEIGHT', 1000.000000, NOW()
    WHERE NOT EXISTS (
    SELECT 1 FROM catalog.units_of_measure WHERE code = 'KG'
);

INSERT INTO catalog.units_of_measure (
    id,
    code,
    name,
    unit_group,
    conversion_factor_to_base,
    created_at
)
SELECT gen_random_uuid(), 'ML', 'Mililitro', 'VOLUME', 1.000000, NOW()
    WHERE NOT EXISTS (
    SELECT 1 FROM catalog.units_of_measure WHERE code = 'ML'
);

INSERT INTO catalog.units_of_measure (
    id,
    code,
    name,
    unit_group,
    conversion_factor_to_base,
    created_at
)
SELECT gen_random_uuid(), 'L', 'Litro', 'VOLUME', 1000.000000, NOW()
    WHERE NOT EXISTS (
    SELECT 1 FROM catalog.units_of_measure WHERE code = 'L'
);

INSERT INTO catalog.units_of_measure (
    id,
    code,
    name,
    unit_group,
    conversion_factor_to_base,
    created_at
)
SELECT gen_random_uuid(), 'PCS', 'Pieza', 'COUNT', 1.000000, NOW()
    WHERE NOT EXISTS (
    SELECT 1 FROM catalog.units_of_measure WHERE code = 'PCS'
);

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM catalog.units_of_measure
        WHERE unit_group IS NULL
           OR conversion_factor_to_base IS NULL
    ) THEN
        RAISE EXCEPTION 'Some units of measure do not have conversion metadata';
END IF;
END $$;

ALTER TABLE catalog.units_of_measure
    ALTER COLUMN unit_group SET NOT NULL;

ALTER TABLE catalog.units_of_measure
    ALTER COLUMN conversion_factor_to_base SET NOT NULL;

ALTER TABLE catalog.units_of_measure
    ADD CONSTRAINT chk_units_of_measure_group
        CHECK (unit_group IN ('WEIGHT', 'VOLUME', 'COUNT'));

ALTER TABLE catalog.units_of_measure
    ADD CONSTRAINT chk_units_of_measure_factor_positive
        CHECK (conversion_factor_to_base > 0);