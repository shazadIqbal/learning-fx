ALTER TABLE customer
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE;

INSERT INTO payment_type (mode)
VALUES
    ('Cash'),
    ('online'),
    ('Credit')
    ON CONFLICT (mode) DO NOTHING;

ALTER TABLE sale
    ADD COLUMN IF NOT EXISTS credit_settled BOOLEAN NOT NULL DEFAULT FALSE;
