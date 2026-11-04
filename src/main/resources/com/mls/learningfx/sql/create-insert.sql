CREATE TABLE users (
                       id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                       name VARCHAR(100) NOT NULL,
                       contact VARCHAR(20),
                       email VARCHAR(150) unique
                           password varchar(150) not null
);



CREATE TABLE customer (
                          id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                          name VARCHAR(100) NOT NULL,
                          contact VARCHAR(20)
);

CREATE TABLE supplier_type (
                               id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                               name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE product_category (
                                  id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                  name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE payment_type (
                              id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                              mode VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE supplier (
                          id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                          name VARCHAR(100) NOT NULL,
                          contact VARCHAR(20),
                          type INTEGER,
                          company_name VARCHAR(150),
                          FOREIGN KEY (type)
                              REFERENCES supplier_type(id)
);

CREATE TABLE product (
                         id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                         name VARCHAR(150) NOT NULL,
                         category_id INTEGER,
                         price NUMERIC(12, 2) NOT NULL CHECK (price >= 0),
                         sell_price NUMERIC(12, 2) NOT NULL CHECK (sell_price >= 0),
                         created_by INTEGER,
                         FOREIGN KEY (category_id)
                             REFERENCES product_category(id),

                         FOREIGN KEY (created_by)
                             REFERENCES users(id)
);

CREATE TABLE stock (
                       id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                       qty INTEGER NOT NULL CHECK (qty >= 0),
                       date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       created_by INTEGER,
                       product_id INTEGER NOT NULL,

                       FOREIGN KEY (created_by)
                           REFERENCES users(id),

                       FOREIGN KEY (product_id)
                           REFERENCES product(id)
);

CREATE TABLE sale (
                      s_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                      cus_id INTEGER,
                      user_id INTEGER,
                      date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      total_price NUMERIC(12, 2) NOT NULL DEFAULT 0
                          CHECK (total_price >= 0),
                      payment_type_id INTEGER,
                      FOREIGN KEY (cus_id)
                          REFERENCES customer(id),

                      CONSTRAINT fk_sale_user
                          FOREIGN KEY (user_id)
                              REFERENCES users(id),

                      CONSTRAINT fk_sale_payment_type
                          FOREIGN KEY (payment_type_id)
                              REFERENCES payment_type(id)
);

CREATE TABLE sale_item (
                           s_id INTEGER NOT NULL,
                           p_id INTEGER NOT NULL,
                           qty INTEGER NOT NULL CHECK (qty > 0),

                           PRIMARY KEY (s_id, p_id),

                           FOREIGN KEY (s_id)
                               REFERENCES sale(s_id),

                           CONSTRAINT fk_sale_item_product
                               FOREIGN KEY (p_id)
                                   REFERENCES product(id)
);

CREATE TABLE product_supplier (
                                  pid INTEGER NOT NULL,
                                  supp_id INTEGER NOT NULL,

                                  PRIMARY KEY (pid, supp_id),

                                  FOREIGN KEY (pid)
                                      REFERENCES product(id),

                                  FOREIGN KEY (supp_id)
                                      REFERENCES supplier(id)
);


INSERT INTO users (name, contact, email,password)
VALUES
    ('Wasi', '03001234567', 'wasi@gmail.com','admin'),
    ('Ali', '03111234567', 'ali@gmail.com','admin'),
    ('Ahmed', '03221234567', 'ahmed@gmail.com','admin'),
    ('Usman', '03331234567', 'usman@gmail.com','admin'),
    ('Hamza', '03451234567', 'hamza@gmail.com','admin');

INSERT INTO customer (name, contact)
VALUES
    ('Bilal', '03001112222'),
    ('Hassan', '03112223333'),
    ('Saad', '03223334444'),
    ('Zain', '03334445555'),
    ('Danish', '03445556666');

INSERT INTO supplier_type (name)
VALUES
    ('Manufacturer'),
    ('Wholesaler'),
    ('Distributor'),
    ('Retailer'),
    ('Importer');

INSERT INTO product_category (name)
VALUES
    ('Electronics'),
    ('Accessories'),
    ('Clothing'),
    ('Groceries'),
    ('Furniture');

INSERT INTO payment_type (mode)
VALUES
    ('Cash'),
    ('online');

INSERT INTO supplier (name, contact, type, company_name)
VALUES
    ('Ali Traders', '03001231234', 1, 'Ali Electronics'),
    ('Ahmed Supplies', '03112341234', 2, 'Ahmed Wholesale'),
    ('Usman Distributors', '03223451234', 3, 'Usman Distribution'),
    ('Bilal Store', '03334561234', 4, 'Bilal Retailers'),
    ('Hassan Imports', '03445671234', 5, 'Hassan Imports Pvt Ltd');

INSERT INTO product (name, category_id, price, sell_price, created_by)
VALUES
    ('Laptop', 1, 120000.00, 135000.00, 1),
    ('Mouse', 2, 800.00, 1200.00, 1),
    ('Keyboard', 2, 1500.00, 2200.00, 2),
    ('T-Shirt', 3, 700.00, 1000.00, 3),
    ('Rice Bag', 4, 2500.00, 3000.00, 2),
    ('Office Chair', 5, 8000.00, 10000.00, 4),
    ('Headphones', 2, 2000.00, 2800.00, 1),
    ('Monitor', 1, 25000.00, 30000.00, 3);

INSERT INTO stock (qty, date, created_by, product_id)
VALUES
    (20, '2026-10-20 10:00:00', 1, 1),
    (100, '2026-10-20 10:30:00', 1, 2),
    (75, '2026-10-21 11:00:00', 2, 3),
    (50, '2026-10-21 12:00:00', 3, 4),
    (40, '2026-10-22 09:00:00', 2, 5),
    (15, '2026-10-22 10:00:00', 4, 6),
    (60, '2026-10-23 11:30:00', 1, 7),
    (25, '2026-10-23 12:00:00', 3, 8);

INSERT INTO sale (cus_id, user_id, date, total_price, payment_type_id)
VALUES
    (1, 1, '2026-10-24 10:00:00', 136200.00, 1),
    (2, 2, '2026-10-24 11:00:00', 3400.00, 2),
    (3, 1, '2026-10-25 12:00:00', 3000.00, 3),
    (4, 3, '2026-10-25 14:00:00', 30000.00, 4),
    (5, 4, '2026-10-26 15:00:00', 10000.00, 5);

INSERT INTO sale_item (s_id, p_id, qty)
VALUES
    (1, 1, 1),
    (1, 2, 1),
    (2, 3, 1),
    (2, 4, 1),
    (3, 5, 1),
    (4, 8, 1),
    (5, 6, 1);

INSERT INTO product_supplier (pid, supp_id)
VALUES
    (1, 1),
    (1, 3),
    (2, 1),
    (2, 2),
    (3, 2),
    (4, 4),
    (5, 3),
    (6, 4),
    (7, 1),
    (8, 5);