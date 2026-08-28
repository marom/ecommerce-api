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

-- product_pictures (2-5 per product). The blobs are tiny 1x1 PNGs (70 bytes each) so this
-- seed file stays small — swap in real photos via POST /api/v1/products/{id}/pictures.
-- display_order 0 is the primary picture.
SET @px_red   = x'89504E470D0A1A0A0000000D49484452000000010000000108060000001F15C4890000000D4944415478DA633860A9FD1F000505022487F12CB50000000049454E44AE426082';
SET @px_green = x'89504E470D0A1A0A0000000D49484452000000010000000108060000001F15C4890000000D4944415478DA63505F97F01F00046A0235D45E8C370000000049454E44AE426082';
SET @px_blue  = x'89504E470D0A1A0A0000000D49484452000000010000000108060000001F15C4890000000D4944415478DA63D06CD8F91F00049A026200095B340000000049454E44AE426082';
SET @px_gray  = x'89504E470D0A1A0A0000000D49484452000000010000000108060000001F15C4890000000D4944415478DA6398BA74D97F00069302E076BCF1DE0000000049454E44AE426082';
SET @px_white = x'89504E470D0A1A0A0000000D49484452000000010000000108060000001F15C4890000000D4944415478DA63F8FFFFFF7F0009FB03FDF5D8F19A0000000049454E44AE426082';

INSERT INTO product_pictures (product_id, data, content_type, size_bytes, original_filename, alt_text, display_order, created_at, updated_at) VALUES
-- 1: Wireless Mouse (4)
(1, @px_gray,  'image/png', 70, 'wireless-mouse-1.png', 'Wireless Mouse - front',      0, NOW(), NOW()),
(1, @px_blue,  'image/png', 70, 'wireless-mouse-2.png', 'Wireless Mouse - side',       1, NOW(), NOW()),
(1, @px_green, 'image/png', 70, 'wireless-mouse-3.png', 'Wireless Mouse - top',        2, NOW(), NOW()),
(1, @px_white, 'image/png', 70, 'wireless-mouse-4.png', 'Wireless Mouse - packaging',  3, NOW(), NOW()),
-- 2: Mechanical Keyboard (5)
(2, @px_gray,  'image/png', 70, 'mechanical-keyboard-1.png', 'Mechanical Keyboard - front',        0, NOW(), NOW()),
(2, @px_blue,  'image/png', 70, 'mechanical-keyboard-2.png', 'Mechanical Keyboard - angled',       1, NOW(), NOW()),
(2, @px_green, 'image/png', 70, 'mechanical-keyboard-3.png', 'Mechanical Keyboard - side profile', 2, NOW(), NOW()),
(2, @px_red,   'image/png', 70, 'mechanical-keyboard-4.png', 'Mechanical Keyboard - keycap detail',3, NOW(), NOW()),
(2, @px_white, 'image/png', 70, 'mechanical-keyboard-5.png', 'Mechanical Keyboard - RGB lighting', 4, NOW(), NOW()),
-- 3: USB-C Hub (3)
(3, @px_gray,  'image/png', 70, 'usb-c-hub-1.png', 'USB-C Hub - front',        0, NOW(), NOW()),
(3, @px_blue,  'image/png', 70, 'usb-c-hub-2.png', 'USB-C Hub - ports detail', 1, NOW(), NOW()),
(3, @px_green, 'image/png', 70, 'usb-c-hub-3.png', 'USB-C Hub - in use',       2, NOW(), NOW()),
-- 4: Noise-Cancelling Headphones (5)
(4, @px_gray,  'image/png', 70, 'headphones-1.png', 'Noise-Cancelling Headphones - front',           0, NOW(), NOW()),
(4, @px_blue,  'image/png', 70, 'headphones-2.png', 'Noise-Cancelling Headphones - folded',          1, NOW(), NOW()),
(4, @px_green, 'image/png', 70, 'headphones-3.png', 'Noise-Cancelling Headphones - ear cushion',     2, NOW(), NOW()),
(4, @px_white, 'image/png', 70, 'headphones-4.png', 'Noise-Cancelling Headphones - carry case',      3, NOW(), NOW()),
(4, @px_red,   'image/png', 70, 'headphones-5.png', 'Noise-Cancelling Headphones - lifestyle',       4, NOW(), NOW()),
-- 5: Clean Code (2)
(5, @px_gray,  'image/png', 70, 'clean-code-1.png', 'Clean Code - front cover', 0, NOW(), NOW()),
(5, @px_blue,  'image/png', 70, 'clean-code-2.png', 'Clean Code - back cover',  1, NOW(), NOW()),
-- 6: The Pragmatic Programmer (2)
(6, @px_gray,  'image/png', 70, 'pragmatic-programmer-1.png', 'The Pragmatic Programmer - front cover', 0, NOW(), NOW()),
(6, @px_blue,  'image/png', 70, 'pragmatic-programmer-2.png', 'The Pragmatic Programmer - spine',       1, NOW(), NOW()),
-- 7: Cotton T-Shirt (4)
(7, @px_white, 'image/png', 70, 'cotton-t-shirt-1.png', 'Cotton T-Shirt - front',        0, NOW(), NOW()),
(7, @px_gray,  'image/png', 70, 'cotton-t-shirt-2.png', 'Cotton T-Shirt - back',         1, NOW(), NOW()),
(7, @px_blue,  'image/png', 70, 'cotton-t-shirt-3.png', 'Cotton T-Shirt - fabric detail',2, NOW(), NOW()),
(7, @px_green, 'image/png', 70, 'cotton-t-shirt-4.png', 'Cotton T-Shirt - folded',       3, NOW(), NOW()),
-- 8: Hooded Sweatshirt (3)
(8, @px_gray,  'image/png', 70, 'hooded-sweatshirt-1.png', 'Hooded Sweatshirt - front',       0, NOW(), NOW()),
(8, @px_blue,  'image/png', 70, 'hooded-sweatshirt-2.png', 'Hooded Sweatshirt - hood detail', 1, NOW(), NOW()),
(8, @px_green, 'image/png', 70, 'hooded-sweatshirt-3.png', 'Hooded Sweatshirt - back',        2, NOW(), NOW()),
-- 9: Stainless Steel Bottle (3)
(9, @px_gray,  'image/png', 70, 'steel-bottle-1.png', 'Stainless Steel Bottle - front',           0, NOW(), NOW()),
(9, @px_blue,  'image/png', 70, 'steel-bottle-2.png', 'Stainless Steel Bottle - lid detail',      1, NOW(), NOW()),
(9, @px_green, 'image/png', 70, 'steel-bottle-3.png', 'Stainless Steel Bottle - size comparison', 2, NOW(), NOW()),
-- 10: Ceramic Coffee Mug (4)
(10, @px_white, 'image/png', 70, 'coffee-mug-1.png', 'Ceramic Coffee Mug - front',        0, NOW(), NOW()),
(10, @px_gray,  'image/png', 70, 'coffee-mug-2.png', 'Ceramic Coffee Mug - handle detail',1, NOW(), NOW()),
(10, @px_blue,  'image/png', 70, 'coffee-mug-3.png', 'Ceramic Coffee Mug - top view',     2, NOW(), NOW()),
(10, @px_green, 'image/png', 70, 'coffee-mug-4.png', 'Ceramic Coffee Mug - boxed',        3, NOW(), NOW());

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
