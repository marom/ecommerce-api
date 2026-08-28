-- Example / demo data for ecommerce_db.
-- Load AFTER db/schema.sql (which creates the database and all tables):
--   mysql -u root < db/schema.sql
--   mysql -u root < db/example-data.sql
--
-- Reference data (categories, products, product pictures) + demo customers and
-- user accounts. Orders, order_items and payments are intentionally left empty —
-- create those through the API so order-number generation, stock reduction and
-- the cascade save all run as designed.

USE ecommerce_db;

-- categories
INSERT INTO categories (name, slug, description, created_at, updated_at) VALUES
('Electronics',    'electronics',    'Gadgets, accessories and devices',   NOW(), NOW()),
('Books',          'books',          'Printed and reference books',        NOW(), NOW()),
('Clothing',       'clothing',       'Apparel and everyday wear',          NOW(), NOW()),
('Home & Kitchen', 'home-kitchen',   'Homeware and kitchen essentials',    NOW(), NOW());

-- products
INSERT INTO products (name, description, price, sku, stock_quantity, active, category_id, created_at, updated_at) VALUES
('Wireless Mouse',              'Ergonomic 2.4GHz wireless mouse',            24.99, 'ELEC-MOU-001', 150, TRUE, 1, NOW(), NOW()),
('Mechanical Keyboard',         'Tactile mechanical keyboard, RGB backlit',   79.99, 'ELEC-KEY-002',  80, TRUE, 1, NOW(), NOW()),
('USB-C Hub',                   '7-in-1 USB-C multiport adapter',             39.99, 'ELEC-HUB-003',  60, TRUE, 1, NOW(), NOW()),
('Noise-Cancelling Headphones', 'Over-ear ANC wireless headphones',          199.99, 'ELEC-HDP-004',  40, TRUE, 1, NOW(), NOW()),
('Clean Code',                  'A Handbook of Agile Software Craftsmanship', 32.50, 'BOOK-CLN-001', 200, TRUE, 2, NOW(), NOW()),
('The Pragmatic Programmer',    'Your Journey to Mastery',                    41.00, 'BOOK-PRG-002', 120, TRUE, 2, NOW(), NOW()),
('Cotton T-Shirt',              'Soft 100% cotton crew-neck tee',             15.00, 'CLTH-TSH-001', 300, TRUE, 3, NOW(), NOW()),
('Hooded Sweatshirt',           'Fleece-lined pullover hoodie',               45.00, 'CLTH-HOD-002',  90, TRUE, 3, NOW(), NOW()),
('Stainless Steel Bottle',      'Insulated 750ml water bottle',               18.99, 'HOME-BTL-001', 250, TRUE, 4, NOW(), NOW()),
('Ceramic Coffee Mug',          '350ml glazed ceramic mug',                   12.50, 'HOME-MUG-002', 180, TRUE, 4, NOW(), NOW());

-- product_pictures: none seeded — upload via POST /api/v1/products/{id}/pictures.

-- customers
INSERT INTO customers (first_name, last_name, email, phone, address, created_at) VALUES
('John',  'Doe',   'john.doe@example.com',   '+1-202-555-0101', '123 Maple Street, Springfield', NOW()),
('Jane',  'Smith', 'jane.smith@example.com', '+1-202-555-0142', '88 Oak Avenue, Riverdale',       NOW()),
('Ravi',  'Kumar', 'ravi.kumar@example.com', '+91-90000-12345', '12 MG Road, Bengaluru',          NOW());

-- users
-- Plaintext passwords (DEMO ONLY):
--   admin@shop.example.com  -> "admin123"     (ROLE_ADMIN, not linked to a customer)
--   john.doe@example.com    -> "password123"  (ROLE_CUSTOMER, customer 1)
--   jane.smith@example.com  -> "password123"  (ROLE_CUSTOMER, customer 2)
--   ravi.kumar@example.com  -> "password123"  (ROLE_CUSTOMER, customer 3)
-- Regenerate a hash with:
--   htpasswd -bnBC 10 "" 'admin123' | tr -d ':\n' | sed 's/^\$2y/\$2a/'
--   (or new BCryptPasswordEncoder().encode("admin123"))
INSERT INTO users (email, password, role, customer_id, enabled, created_at, updated_at) VALUES
('admin@shop.example.com', '$2a$10$s6yUkD9fsw5tMpiVnu4Xwe1hBYuVL4345DHJeCE1xqfThZ4s46RwS', 'ROLE_ADMIN',    NULL, TRUE, NOW(), NOW()),
('john.doe@example.com',   '$2a$10$GETQiwsC.fx2V45UMXWAdeBU596dtFeSZstdQXuFyf2y4A1dw4o/O', 'ROLE_CUSTOMER', 1,    TRUE, NOW(), NOW()),
('jane.smith@example.com', '$2a$10$GETQiwsC.fx2V45UMXWAdeBU596dtFeSZstdQXuFyf2y4A1dw4o/O', 'ROLE_CUSTOMER', 2,    TRUE, NOW(), NOW()),
('ravi.kumar@example.com', '$2a$10$GETQiwsC.fx2V45UMXWAdeBU596dtFeSZstdQXuFyf2y4A1dw4o/O', 'ROLE_CUSTOMER', 3,    TRUE, NOW(), NOW());

-- reviews
INSERT INTO reviews (product_id, customer_id, rating, comment, created_at) VALUES
(1, 1, 5, 'Great mouse, very responsive and comfortable to use daily.', NOW()),
(1, 2, 4, 'Works well but the scroll wheel feels a bit loose.', NOW()),
(2, 1, 5, 'Best mechanical keyboard I have owned, love the RGB backlighting.', NOW()),
(5, 3, 5, 'A must-read for every software engineer, highly recommended.', NOW());

-- ---------------------------------------------------------------------------
-- Verification (run after this script to confirm the seed loaded)
-- ---------------------------------------------------------------------------
SELECT * FROM categories;
SELECT * FROM products;
SELECT * FROM product_pictures;
SELECT * FROM customers;
SELECT * FROM users;
SELECT * FROM orders;
SELECT * FROM order_items;
SELECT * FROM payments;
SELECT * FROM reviews;
