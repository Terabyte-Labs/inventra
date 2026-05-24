ALTER TABLE manufacturing.manufacturing_order_inputs
    ADD COLUMN inventory_movement_id UUID REFERENCES inventory.inventory_movements(id);