CREATE TABLE IF NOT EXISTS tax_config (
    id UUID PRIMARY KEY,
    branch_id UUID NOT NULL,
    tax_rate NUMERIC(6,4) NOT NULL,
    updated_by UUID NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_tax_config_branch FOREIGN KEY (branch_id) REFERENCES branch (id),
    CONSTRAINT fk_tax_config_updated_by FOREIGN KEY (updated_by) REFERENCES app_user (id),
    CONSTRAINT uq_tax_config_branch UNIQUE (branch_id)
);

CREATE TABLE IF NOT EXISTS invoice (
    id UUID PRIMARY KEY,
    branch_id UUID NOT NULL,
    visit_id UUID NOT NULL,
    invoice_number VARCHAR(40) NOT NULL,
    status VARCHAR(20) NOT NULL,
    items_subtotal NUMERIC(12,2) NOT NULL,
    discount_amount NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    tax_rate NUMERIC(6,4) NOT NULL,
    tax_amount NUMERIC(12,2) NOT NULL,
    total NUMERIC(12,2) NOT NULL,
    void_reason VARCHAR(255) NULL,
    voided_at TIMESTAMPTZ NULL,
    created_by UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_invoice_branch FOREIGN KEY (branch_id) REFERENCES branch (id),
    CONSTRAINT fk_invoice_visit FOREIGN KEY (visit_id) REFERENCES visit (id),
    CONSTRAINT fk_invoice_created_by FOREIGN KEY (created_by) REFERENCES app_user (id)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_invoice_branch_number ON invoice (branch_id, invoice_number);
CREATE INDEX IF NOT EXISTS idx_invoice_branch_status_created ON invoice (branch_id, status, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_invoice_branch_visit_created ON invoice (branch_id, visit_id, created_at DESC);

CREATE TABLE IF NOT EXISTS invoice_item (
    id UUID PRIMARY KEY,
    branch_id UUID NOT NULL,
    invoice_id UUID NOT NULL,
    item_type VARCHAR(20) NOT NULL,
    item_id UUID NOT NULL,
    description VARCHAR(200) NOT NULL,
    qty NUMERIC(12,3) NOT NULL,
    unit_price NUMERIC(12,2) NOT NULL,
    discount_amount NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    line_total NUMERIC(12,2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_invoice_item_branch FOREIGN KEY (branch_id) REFERENCES branch (id),
    CONSTRAINT fk_invoice_item_invoice FOREIGN KEY (invoice_id) REFERENCES invoice (id)
);

CREATE INDEX IF NOT EXISTS idx_invoice_item_branch_invoice_created ON invoice_item (branch_id, invoice_id, created_at);

CREATE TABLE IF NOT EXISTS invoice_payment (
    id UUID PRIMARY KEY,
    branch_id UUID NOT NULL,
    invoice_id UUID NOT NULL,
    method VARCHAR(20) NOT NULL,
    amount NUMERIC(12,2) NOT NULL,
    reference VARCHAR(80) NULL,
    created_by UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_invoice_payment_branch FOREIGN KEY (branch_id) REFERENCES branch (id),
    CONSTRAINT fk_invoice_payment_invoice FOREIGN KEY (invoice_id) REFERENCES invoice (id),
    CONSTRAINT fk_invoice_payment_created_by FOREIGN KEY (created_by) REFERENCES app_user (id)
);

CREATE INDEX IF NOT EXISTS idx_invoice_payment_branch_invoice_created ON invoice_payment (branch_id, invoice_id, created_at);

CREATE TABLE IF NOT EXISTS invoice_counter (
    branch_id UUID PRIMARY KEY,
    next_number INT NOT NULL,
    CONSTRAINT fk_invoice_counter_branch FOREIGN KEY (branch_id) REFERENCES branch (id)
);
