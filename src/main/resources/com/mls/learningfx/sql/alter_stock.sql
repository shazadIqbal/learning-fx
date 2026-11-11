ALTER TABLE stock
    ADD COLUMN supplier_id INTEGER
    REFERENCES supplier(id);