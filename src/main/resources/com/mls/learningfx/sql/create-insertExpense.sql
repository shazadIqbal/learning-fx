CREATE TABLE expense (
    id           INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    detail       VARCHAR(255) NOT NULL,
    amount       NUMERIC(12,2) NOT NULL CHECK (amount > 0),
    expense_date DATE NOT NULL DEFAULT CURRENT_DATE
);

select * from expense

INSERT INTO expense (detail, amount, expense_date) VALUES
('Shop rent',        2500.00, '2026-11-01'),
('Bijli ka bill',     3500.50, '2026-11-05'),
('Internet bill',     1000.00, '2026-11-07'),
('Staff salary',     40000.00, '2026-11-10'),
('Chai aur nashta',    750.00, '2026-11-12');