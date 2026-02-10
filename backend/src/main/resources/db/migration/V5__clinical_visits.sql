CREATE TABLE IF NOT EXISTS visit (
    id UUID PRIMARY KEY,
    branch_id UUID NOT NULL,
    pet_id UUID NOT NULL,
    service_id UUID NOT NULL,
    appointment_id UUID NULL,
    status VARCHAR(20) NOT NULL,
    s_reason TEXT NOT NULL,
    s_anamnesis TEXT NOT NULL,
    o_weight_kg NUMERIC(6,2) NULL,
    o_temperature_c NUMERIC(4,1) NULL,
    o_findings TEXT NULL,
    a_diagnosis TEXT NULL,
    a_severity VARCHAR(30) NULL,
    p_treatment TEXT NULL,
    p_instructions TEXT NULL,
    p_followup_at DATE NULL,
    created_by UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_visit_branch FOREIGN KEY (branch_id) REFERENCES branch (id),
    CONSTRAINT fk_visit_pet FOREIGN KEY (pet_id) REFERENCES pet (id),
    CONSTRAINT fk_visit_service FOREIGN KEY (service_id) REFERENCES service (id),
    CONSTRAINT fk_visit_appointment FOREIGN KEY (appointment_id) REFERENCES appointment (id),
    CONSTRAINT fk_visit_created_by FOREIGN KEY (created_by) REFERENCES app_user (id)
);

CREATE INDEX IF NOT EXISTS idx_visit_branch_pet_created_at ON visit (branch_id, pet_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_visit_branch_status_created_at ON visit (branch_id, status, created_at DESC);

CREATE TABLE IF NOT EXISTS soap_template (
    id UUID PRIMARY KEY,
    branch_id UUID NOT NULL,
    service_id UUID NOT NULL,
    name VARCHAR(120) NOT NULL,
    s_reason TEXT NULL,
    s_anamnesis TEXT NULL,
    o_findings TEXT NULL,
    a_diagnosis TEXT NULL,
    p_treatment TEXT NULL,
    p_instructions TEXT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_soap_template_branch FOREIGN KEY (branch_id) REFERENCES branch (id),
    CONSTRAINT fk_soap_template_service FOREIGN KEY (service_id) REFERENCES service (id)
);

CREATE INDEX IF NOT EXISTS idx_soap_template_branch_service_active ON soap_template (branch_id, service_id, is_active);

CREATE TABLE IF NOT EXISTS prescription (
    id UUID PRIMARY KEY,
    branch_id UUID NOT NULL,
    visit_id UUID NOT NULL,
    medication VARCHAR(160) NOT NULL,
    dose VARCHAR(60) NOT NULL,
    unit VARCHAR(30) NOT NULL,
    frequency VARCHAR(60) NOT NULL,
    duration VARCHAR(60) NOT NULL,
    route VARCHAR(60) NOT NULL,
    notes TEXT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_prescription_branch FOREIGN KEY (branch_id) REFERENCES branch (id),
    CONSTRAINT fk_prescription_visit FOREIGN KEY (visit_id) REFERENCES visit (id)
);

CREATE INDEX IF NOT EXISTS idx_prescription_branch_visit_created_at ON prescription (branch_id, visit_id, created_at);

CREATE TABLE IF NOT EXISTS visit_attachment (
    id UUID PRIMARY KEY,
    branch_id UUID NOT NULL,
    visit_id UUID NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(80) NOT NULL,
    size_bytes BIGINT NOT NULL,
    storage_path VARCHAR(500) NOT NULL,
    created_by UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_visit_attachment_branch FOREIGN KEY (branch_id) REFERENCES branch (id),
    CONSTRAINT fk_visit_attachment_visit FOREIGN KEY (visit_id) REFERENCES visit (id),
    CONSTRAINT fk_visit_attachment_created_by FOREIGN KEY (created_by) REFERENCES app_user (id)
);

CREATE INDEX IF NOT EXISTS idx_visit_attachment_branch_visit_created_at ON visit_attachment (branch_id, visit_id, created_at);
