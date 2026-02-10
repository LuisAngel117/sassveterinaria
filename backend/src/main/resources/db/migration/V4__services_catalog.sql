ALTER TABLE service
    ADD COLUMN IF NOT EXISTS price_base NUMERIC(12,2);

ALTER TABLE service
    ALTER COLUMN duration_minutes SET DEFAULT 30;

UPDATE service
SET price_base = 20.00
WHERE price_base IS NULL
  AND LOWER(name) = 'consulta general';

UPDATE service
SET price_base = 15.00
WHERE price_base IS NULL
  AND LOWER(name) = 'vacunacion';

UPDATE service
SET price_base = 18.00
WHERE price_base IS NULL
  AND LOWER(name) = 'control post-operatorio';

UPDATE service
SET price_base = 0.00
WHERE price_base IS NULL;

ALTER TABLE service
    ALTER COLUMN price_base SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_service_branch_active ON service (branch_id, is_active);
