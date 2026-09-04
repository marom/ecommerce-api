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
-- Demo reviews for one product ("Hooded Sweatshirt", product id 8) so the AI
-- review-summary endpoint has a meaty product to digest. The reviews table has a
-- UNIQUE (product_id, customer_id) key, so 50 extra reviewers were added too.
-- These extra customers have no login (no row in `users`) -- they exist purely
-- to make the review corpus realistic.
-- NOTE: reviewer ids below assume this file runs right after db/schema.sql on a
-- fresh database (demo customers John/Jane/Ravi take ids 1-3).
-- ---------------------------------------------------------------------------
INSERT INTO customers (first_name, last_name, email, phone, address, created_at) VALUES
('Aisha', 'Chen', 'aisha.chen.1@example.com', '+1-229-482-3112', '100 Cedar St, Lakeside', NOW()),
('Liam', 'Patel', 'liam.patel.2@example.com', '+1-674-751-9193', '64 Pine St, Riverdale', NOW()),
('Noah', 'Garcia', 'noah.garcia.3@example.com', '+1-943-908-8170', '366 Elm St, Riverdale', NOW()),
('Olivia', 'Nguyen', 'olivia.nguyen.4@example.com', '+1-615-325-2194', '136 Pine St, Fairview', NOW()),
('Emma', 'Smith', 'emma.smith.5@example.com', '+1-866-889-9327', '905 Elm St, Georgetown', NOW()),
('Ethan', 'Kim', 'ethan.kim.6@example.com', '+1-971-343-3827', '138 Cedar St, Fairview', NOW()),
('Mia', 'Brown', 'mia.brown.7@example.com', '+1-707-775-6841', '785 Maple St, Riverdale', NOW()),
('Lucas', 'Davis', 'lucas.davis.8@example.com', '+1-672-824-3711', '610 Main St, Springfield', NOW()),
('Sofia', 'Miller', 'sofia.miller.9@example.com', '+1-219-889-4750', '999 Oak St, Springfield', NOW()),
('Mason', 'Wilson', 'mason.wilson.10@example.com', '+1-604-571-1148', '775 Maple St, Georgetown', NOW()),
('Isabella', 'Moore', 'isabella.moore.11@example.com', '+1-355-569-3806', '127 Maple St, Fairview', NOW()),
('James', 'Taylor', 'james.taylor.12@example.com', '+1-416-236-7049', '357 Maple St, Georgetown', NOW()),
('Amelia', 'Anderson', 'amelia.anderson.13@example.com', '+1-921-834-1730', '697 Pine St, Georgetown', NOW()),
('Benjamin', 'Thomas', 'benjamin.thomas.14@example.com', '+1-983-953-6871', '584 Cedar St, Lakeside', NOW()),
('Harper', 'Jackson', 'harper.jackson.15@example.com', '+1-341-913-1461', '255 Elm St, Springfield', NOW()),
('Elijah', 'White', 'elijah.white.16@example.com', '+1-235-982-8004', '922 Main St, Springfield', NOW()),
('Evelyn', 'Harris', 'evelyn.harris.17@example.com', '+1-336-627-2784', '448 Cedar St, Georgetown', NOW()),
('Daniel', 'Martin', 'daniel.martin.18@example.com', '+1-836-613-3994', '429 Cedar St, Georgetown', NOW()),
('Abigail', 'Thompson', 'abigail.thompson.19@example.com', '+1-263-617-7199', '149 Pine St, Springfield', NOW()),
('Matthew', 'Garcia', 'matthew.garcia.20@example.com', '+1-280-932-7885', '145 Cedar St, Lakeside', NOW()),
('Emily', 'Martinez', 'emily.martinez.21@example.com', '+1-935-972-1826', '775 Oak St, Fairview', NOW()),
('Henry', 'Robinson', 'henry.robinson.22@example.com', '+1-909-276-1827', '640 Pine St, Riverdale', NOW()),
('Ella', 'Clark', 'ella.clark.23@example.com', '+1-280-595-9664', '32 Maple St, Springfield', NOW()),
('Alexander', 'Rodriguez', 'alexander.rodriguez.24@example.com', '+1-522-690-7707', '105 Main St, Lakeside', NOW()),
('Scarlett', 'Lewis', 'scarlett.lewis.25@example.com', '+1-470-860-6326', '718 Maple St, Springfield', NOW()),
('Sebastian', 'Lee', 'sebastian.lee.26@example.com', '+1-678-429-4701', '743 Elm St, Lakeside', NOW()),
('Grace', 'Walker', 'grace.walker.27@example.com', '+1-529-862-7398', '403 Oak St, Lakeside', NOW()),
('Jack', 'Hall', 'jack.hall.28@example.com', '+1-316-276-1453', '591 Maple St, Lakeside', NOW()),
('Chloe', 'Allen', 'chloe.allen.29@example.com', '+1-687-742-7339', '672 Pine St, Riverdale', NOW()),
('Owen', 'Young', 'owen.young.30@example.com', '+1-412-458-8417', '445 Elm St, Georgetown', NOW()),
('Zoe', 'Hernandez', 'zoe.hernandez.31@example.com', '+1-422-768-3920', '962 Cedar St, Fairview', NOW()),
('Leo', 'King', 'leo.king.32@example.com', '+1-233-808-5891', '414 Main St, Lakeside', NOW()),
('Layla', 'Wright', 'layla.wright.33@example.com', '+1-481-339-7266', '123 Oak St, Georgetown', NOW()),
('Julian', 'Lopez', 'julian.lopez.34@example.com', '+1-367-526-7699', '961 Main St, Georgetown', NOW()),
('Nora', 'Hill', 'nora.hill.35@example.com', '+1-733-460-2313', '203 Main St, Lakeside', NOW()),
('Gabriel', 'Scott', 'gabriel.scott.36@example.com', '+1-865-420-9962', '244 Oak St, Lakeside', NOW()),
('Riley', 'Green', 'riley.green.37@example.com', '+1-987-397-4346', '495 Pine St, Georgetown', NOW()),
('Carter', 'Adams', 'carter.adams.38@example.com', '+1-767-311-3657', '712 Pine St, Lakeside', NOW()),
('Aria', 'Baker', 'aria.baker.39@example.com', '+1-986-618-1192', '949 Oak St, Lakeside', NOW()),
('Wyatt', 'Gonzalez', 'wyatt.gonzalez.40@example.com', '+1-536-910-8587', '738 Main St, Fairview', NOW()),
('Hannah', 'Nelson', 'hannah.nelson.41@example.com', '+1-753-628-5746', '908 Main St, Riverdale', NOW()),
('Caleb', 'Carter', 'caleb.carter.42@example.com', '+1-629-450-3837', '537 Main St, Springfield', NOW()),
('Avery', 'Mitchell', 'avery.mitchell.43@example.com', '+1-775-380-7777', '272 Cedar St, Springfield', NOW()),
('Nathan', 'Perez', 'nathan.perez.44@example.com', '+1-363-527-2644', '126 Pine St, Georgetown', NOW()),
('Eleanor', 'Roberts', 'eleanor.roberts.45@example.com', '+1-613-501-2114', '209 Elm St, Georgetown', NOW()),
('Isaac', 'Turner', 'isaac.turner.46@example.com', '+1-764-275-5874', '188 Pine St, Georgetown', NOW()),
('Stella', 'Phillips', 'stella.phillips.47@example.com', '+1-924-352-5149', '633 Elm St, Lakeside', NOW()),
('Dylan', 'Campbell', 'dylan.campbell.48@example.com', '+1-394-223-4867', '269 Maple St, Riverdale', NOW()),
('Lucy', 'Parker', 'lucy.parker.49@example.com', '+1-544-362-3981', '831 Elm St, Lakeside', NOW()),
('Adam', 'Evans', 'adam.evans.50@example.com', '+1-405-676-1927', '568 Oak St, Springfield', NOW());

INSERT INTO reviews (product_id, customer_id, rating, comment, created_at) VALUES
(8, 4, 3, 'Fine quality, though the hood could be a little bigger.', NOW()),
(8, 5, 3, 'It''s okay - soft but the seams started to fray slightly.', NOW()),
(8, 6, 4, 'Average hoodie, comparable to others at this price.', NOW()),
(8, 7, 2, 'Color bled onto other clothes in the wash.', NOW()),
(8, 8, 5, 'Love the fit and the weight of the fabric, not flimsy at all.', NOW()),
(8, 9, 2, 'Runs small, had to return for a bigger size.', NOW()),
(8, 10, 5, 'Holds up well in the dryer, no pilling so far.', NOW()),
(8, 11, 5, 'Great quality for the price, the fabric is thick and soft.', NOW()),
(8, 12, 5, 'Great hoodie, sizing chart was spot on.', NOW()),
(8, 13, 2, 'Not worth the money, quality is mediocre.', NOW()),
(8, 14, 4, 'Bought one for myself and another as a gift. Both great.', NOW()),
(8, 15, 4, 'Exactly as pictured, great value.', NOW()),
(8, 16, 2, 'Too thin to be warm, feels more like a long-sleeve tee.', NOW()),
(8, 17, 3, 'Good basic hoodie, nothing fancy but does the job.', NOW()),
(8, 18, 2, 'Sheds lint everywhere, very annoying.', NOW()),
(8, 19, 4, 'Average hoodie, comparable to others at this price.', NOW()),
(8, 20, 3, 'Solid buy but nothing special.', NOW()),
(8, 21, 3, 'Took a while to soften up but it''s fine now.', NOW()),
(8, 22, 5, 'Fabric is high quality and the stitching looks neat.', NOW()),
(8, 23, 5, 'Great hoodie, sizing chart was spot on.', NOW()),
(8, 24, 2, 'Fabric pilled after only two weeks, disappointing.', NOW()),
(8, 25, 2, 'Sheds lint everywhere, very annoying.', NOW()),
(8, 26, 5, 'Great quality for the price, the fabric is thick and soft.', NOW()),
(8, 27, 4, 'Hood is roomy and the cuffs are snug without being tight.', NOW()),
(8, 28, 5, 'Bought one for myself and another as a gift. Both great.', NOW()),
(8, 29, 4, 'Excellent everyday hoodie, color hasn''t faded after a month.', NOW()),
(8, 30, 5, 'Nice deep pockets and a sturdy zip.', NOW()),
(8, 31, 2, 'Not worth the money, quality is mediocre.', NOW()),
(8, 32, 5, 'Durable and comfortable, used it daily for weeks.', NOW()),
(8, 33, 5, 'Super soft inside, worth every cent.', NOW()),
(8, 34, 5, 'Received tons of compliments on the color.', NOW()),
(8, 35, 1, 'Color bled onto other clothes in the wash.', NOW()),
(8, 36, 5, 'Warm and comfortable; I basically live in it on weekends.', NOW()),
(8, 37, 3, 'Decent hoodie but the color is a bit darker than the photos.', NOW()),
(8, 38, 5, 'Durable and comfortable, used it daily for weeks.', NOW()),
(8, 39, 2, 'The stitching came loose around the pocket after a month.', NOW()),
(8, 40, 3, 'It''s okay - soft but the seams started to fray slightly.', NOW()),
(8, 41, 3, 'Good basic hoodie, nothing fancy but does the job.', NOW()),
(8, 42, 5, 'Holds up well in the dryer, no pilling so far.', NOW()),
(8, 43, 2, 'Color bled onto other clothes in the wash.', NOW()),
(8, 44, 5, 'Excellent everyday hoodie, color hasn''t faded after a month.', NOW()),
(8, 45, 4, 'Perfect weight for cool evenings.', NOW()),
(8, 46, 3, 'Decent hoodie but the color is a bit darker than the photos.', NOW()),
(8, 47, 2, 'Arrived with a loose thread and the seams look uneven.', NOW()),
(8, 48, 5, 'Great quality for the price, the fabric is thick and soft.', NOW()),
(8, 49, 3, 'Solid buy but nothing special.', NOW()),
(8, 50, 5, 'Fits perfectly and the hood keeps its shape after washing.', NOW()),
(8, 51, 4, 'Good basic hoodie, nothing fancy but does the job.', NOW()),
(8, 52, 5, 'Great quality for the price, the fabric is thick and soft.', NOW()),
(8, 53, 5, 'Perfect weight for cool evenings.', NOW());

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
