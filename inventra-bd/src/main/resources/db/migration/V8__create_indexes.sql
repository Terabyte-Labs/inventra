CREATE INDEX idx_products_name
    ON catalog.products(name);

CREATE INDEX idx_products_sku
    ON catalog.products(sku);

CREATE INDEX idx_products_type
    ON catalog.products(product_type);

CREATE INDEX idx_stock_product
    ON inventory.stock_balances(product_id);

CREATE INDEX idx_stock_warehouse
    ON inventory.stock_balances(warehouse_id);

CREATE INDEX idx_movements_product
    ON inventory.inventory_movements(product_id);

CREATE INDEX idx_movements_warehouse
    ON inventory.inventory_movements(warehouse_id);

CREATE INDEX idx_movements_created_at
    ON inventory.inventory_movements(created_at);

CREATE INDEX idx_orders_status
    ON manufacturing.manufacturing_orders(status);

CREATE INDEX idx_orders_product
    ON manufacturing.manufacturing_orders(product_id);