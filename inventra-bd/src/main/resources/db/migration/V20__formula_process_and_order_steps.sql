CREATE TABLE IF NOT EXISTS manufacturing.formula_process_steps (
    id UUID PRIMARY KEY,
    formula_id UUID NOT NULL,
    step_number INTEGER NOT NULL,
    name VARCHAR(160) NOT NULL,
    description TEXT,
    step_type VARCHAR(60) NOT NULL,
    requires_quality_check BOOLEAN NOT NULL,
    expected_duration_minutes INTEGER,
    active BOOLEAN NOT NULL,
    CONSTRAINT fk_formula_process_steps_formula
        FOREIGN KEY (formula_id)
        REFERENCES manufacturing.formulas (id),
    CONSTRAINT uq_formula_process_step_number
        UNIQUE (formula_id, step_number)
);

ALTER TABLE manufacturing.formula_items
    ADD COLUMN IF NOT EXISTS process_step_id UUID;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_formula_items_process_step'
    ) THEN
        ALTER TABLE manufacturing.formula_items
            ADD CONSTRAINT fk_formula_items_process_step
            FOREIGN KEY (process_step_id)
            REFERENCES manufacturing.formula_process_steps (id);
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS manufacturing.manufacturing_order_steps (
    id UUID PRIMARY KEY,
    manufacturing_order_id UUID NOT NULL,
    step_number INTEGER NOT NULL,
    name VARCHAR(160) NOT NULL,
    description TEXT,
    step_type VARCHAR(60) NOT NULL,
    status VARCHAR(40) NOT NULL,
    requires_quality_check BOOLEAN NOT NULL,
    expected_duration_minutes INTEGER,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    quality_result VARCHAR(40),
    notes TEXT,
    CONSTRAINT fk_manufacturing_order_steps_order
        FOREIGN KEY (manufacturing_order_id)
        REFERENCES manufacturing.manufacturing_orders (id),
    CONSTRAINT uq_manufacturing_order_step_number
        UNIQUE (manufacturing_order_id, step_number)
);
