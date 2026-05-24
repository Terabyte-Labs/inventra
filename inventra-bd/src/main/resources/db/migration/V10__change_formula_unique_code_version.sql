ALTER TABLE manufacturing.formulas
DROP CONSTRAINT formulas_code_key;

ALTER TABLE manufacturing.formulas
    ADD CONSTRAINT uq_formula_code_version UNIQUE (code, version);