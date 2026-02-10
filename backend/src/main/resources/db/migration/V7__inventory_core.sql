CREATE TABLE IF NOT EXISTS unit (
    id UUID PRIMARY KEY,
    code VARCHAR(30) NOT NULL,
    name VARCHAR(80) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_unit_code ON unit (code);

CREATE TABLE IF NOT EXISTS product (
    id UUID PRIMARY KEY,
    branch_id UUID NOT NULL,
    sku VARCHAR(60) NULL,
    name VARCHAR(160) NOT NULL,
    unit_id UUID NOT NULL,
    min_qty NUMERIC(12,3) NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_product_branch FOREIGN KEY (branch_id) REFERENCES branch (id),
    CONSTRAINT fk_product_unit FOREIGN KEY (unit_id) REFERENCES unit (id)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_product_branch_sku
    ON product (branch_id, sku)
    WHERE sku IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_product_branch_name ON product (branch_id, name);
CREATE INDEX IF NOT EXISTS idx_product_branch_active_name ON product (branch_id, is_active, name);

CREATE TABLE IF NOT EXISTS product_stock (
    id UUID PRIMARY KEY,
    branch_id UUID NOT NULL,
    product_id UUID NOT NULL,
    on_hand_qty NUMERIC(12,3) NOT NULL DEFAULT 0,
    avg_unit_cost NUMERIC(12,4) NOT NULL DEFAULT 0.0000,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_product_stock_branch FOREIGN KEY (branch_id) REFERENCES branch (id),
    CONSTRAINT fk_product_stock_product FOREIGN KEY (product_id) REFERENCES product (id),
    CONSTRAINT uq_product_stock_branch_product UNIQUE (branch_id, product_id)
);

CREATE INDEX IF NOT EXISTS idx_product_stock_branch_qty ON product_stock (branch_id, on_hand_qty);

CREATE TABLE IF NOT EXISTS stock_movement (
    id UUID PRIMARY KEY,
    branch_id UUID NOT NULL,
    product_id UUID NOT NULL,
    type VARCHAR(20) NOT NULL,
    qty NUMERIC(12,3) NOT NULL,
    unit_cost NUMERIC(12,4) NULL,
    total_cost NUMERIC(12,4) NOT NULL,
    reason VARCHAR(255) NULL,
    visit_id UUID NULL,
    created_by UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_stock_movement_branch FOREIGN KEY (branch_id) REFERENCES branch (id),
    CONSTRAINT fk_stock_movement_product FOREIGN KEY (product_id) REFERENCES product (id),
    CONSTRAINT fk_stock_movement_visit FOREIGN KEY (visit_id) REFERENCES visit (id),
    CONSTRAINT fk_stock_movement_created_by FOREIGN KEY (created_by) REFERENCES app_user (id),
    CONSTRAINT ck_stock_movement_type CHECK (type IN ('IN', 'OUT', 'ADJUST', 'CONSUME')),
    CONSTRAINT ck_stock_movement_qty_positive CHECK (qty > 0)
);

CREATE INDEX IF NOT EXISTS idx_stock_movement_branch_product_created
    ON stock_movement (branch_id, product_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_stock_movement_branch_type_created
    ON stock_movement (branch_id, type, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_stock_movement_branch_visit
    ON stock_movement (branch_id, visit_id);

CREATE TABLE IF NOT EXISTS service_bom_item (
    id UUID PRIMARY KEY,
    branch_id UUID NOT NULL,
    service_id UUID NOT NULL,
    product_id UUID NOT NULL,
    qty NUMERIC(12,3) NOT NULL,
    CONSTRAINT fk_service_bom_branch FOREIGN KEY (branch_id) REFERENCES branch (id),
    CONSTRAINT fk_service_bom_service FOREIGN KEY (service_id) REFERENCES service (id),
    CONSTRAINT fk_service_bom_product FOREIGN KEY (product_id) REFERENCES product (id),
    CONSTRAINT uq_service_bom_branch_service_product UNIQUE (branch_id, service_id, product_id),
    CONSTRAINT ck_service_bom_qty_positive CHECK (qty > 0)
);

CREATE INDEX IF NOT EXISTS idx_service_bom_branch_service ON service_bom_item (branch_id, service_id);
