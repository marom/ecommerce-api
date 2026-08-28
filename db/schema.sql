-- Database structure only (DDL). Demo/seed rows live in db/example-data.sql,
-- which must be loaded after this file.
--
-- Reference:
-- https://docs.fedoraproject.org/en-US/quick-docs/installing-mysql-mariadb/
DROP DATABASE IF EXISTS ecommerce_db;
CREATE DATABASE ecommerce_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ecommerce_db;

-- categories
CREATE TABLE categories (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100)  NOT NULL,
    slug        VARCHAR(100)  NOT NULL,
    description TEXT,
    created_at  DATETIME,
    updated_at  DATETIME,
    PRIMARY KEY (id),
    UNIQUE KEY uk_categories_name (name),
    UNIQUE KEY uk_categories_slug (slug)
);

-- products
CREATE TABLE products (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    name           VARCHAR(200)  NOT NULL,
    description    TEXT,
    price          DECIMAL(10,2) NOT NULL,
    sku            VARCHAR(100)  NOT NULL,
    stock_quantity INT           NOT NULL DEFAULT 0,
    active         BOOLEAN       NOT NULL DEFAULT TRUE,
    category_id    BIGINT        NOT NULL,
    created_at     DATETIME,
    updated_at     DATETIME,
    PRIMARY KEY (id),
    UNIQUE KEY uk_products_sku (sku),
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories (id)
);

-- product_pictures
CREATE TABLE product_pictures (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    product_id        BIGINT       NOT NULL,
    data              LONGBLOB     NOT NULL,          -- raw image bytes
    content_type      VARCHAR(100) NOT NULL,          -- 'image/jpeg' | 'image/png' | 'image/webp'
    size_bytes        BIGINT       NOT NULL,
    original_filename VARCHAR(255),
    alt_text          VARCHAR(255),
    display_order     INT          NOT NULL DEFAULT 0, -- lowest value is the primary picture
    created_at        DATETIME,
    updated_at        DATETIME,
    PRIMARY KEY (id),
    CONSTRAINT fk_product_pictures_product FOREIGN KEY (product_id) REFERENCES products (id)
);

-- customers
CREATE TABLE customers (
    id         BIGINT        NOT NULL AUTO_INCREMENT,
    first_name VARCHAR(100)  NOT NULL,
    last_name  VARCHAR(100)  NOT NULL,
    email      VARCHAR(255)  NOT NULL,
    phone      VARCHAR(20),
    address    TEXT,
    created_at DATETIME,
    PRIMARY KEY (id),
    UNIQUE KEY uk_customers_email (email)
);

-- users (authentication / authorization)
CREATE TABLE users (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    email       VARCHAR(255)  NOT NULL,
    password    VARCHAR(100)  NOT NULL,          -- BCrypt hash ($2a$, 60 chars); 100 for headroom
    role        VARCHAR(20)   NOT NULL,          -- 'ROLE_ADMIN' | 'ROLE_CUSTOMER'
    customer_id BIGINT,                          -- NULL for ADMIN, set for CUSTOMER
    enabled     BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at  DATETIME,
    updated_at  DATETIME,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email),
    UNIQUE KEY uk_users_customer_id (customer_id),
    CONSTRAINT fk_users_customer FOREIGN KEY (customer_id) REFERENCES customers (id)
);

-- orders
CREATE TABLE orders (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    order_number     VARCHAR(20)   NOT NULL,
    customer_id      BIGINT        NOT NULL,
    status           VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    total_amount     DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    shipping_address TEXT          NOT NULL,
    notes            TEXT,
    created_at       DATETIME,
    updated_at       DATETIME,
    PRIMARY KEY (id),
    UNIQUE KEY uk_orders_order_number (order_number),
    CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customers (id)
);

-- order_items
CREATE TABLE order_items (
    id         BIGINT        NOT NULL AUTO_INCREMENT,
    order_id   BIGINT        NOT NULL,
    product_id BIGINT        NOT NULL,
    quantity   INT           NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    subtotal   DECIMAL(12,2) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_order_items_order   FOREIGN KEY (order_id)   REFERENCES orders   (id),
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products (id)
);

-- payments
CREATE TABLE payments (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    order_id       BIGINT        NOT NULL,
    payment_method VARCHAR(30)   NOT NULL,
    payment_status VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    amount         DECIMAL(12,2) NOT NULL,
    created_at     DATETIME,
    PRIMARY KEY (id),
    UNIQUE KEY uk_payments_order_id (order_id),
    CONSTRAINT fk_payments_order FOREIGN KEY (order_id) REFERENCES orders (id)
);

-- reviews
CREATE TABLE reviews (
    id          BIGINT NOT NULL AUTO_INCREMENT,
    product_id  BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    rating      INT    NOT NULL,
    comment     TEXT   NOT NULL,
    created_at  DATETIME,
    PRIMARY KEY (id),
    UNIQUE KEY uk_reviews_product_customer (product_id, customer_id),
    CONSTRAINT fk_reviews_product  FOREIGN KEY (product_id)  REFERENCES products  (id),
    CONSTRAINT fk_reviews_customer FOREIGN KEY (customer_id) REFERENCES customers (id)
);

-- indexes
CREATE INDEX idx_products_category        ON products         (category_id);
CREATE INDEX idx_products_active          ON products         (active);
CREATE INDEX idx_product_pictures_product ON product_pictures (product_id);
CREATE INDEX idx_orders_customer          ON orders           (customer_id);
CREATE INDEX idx_orders_status            ON orders           (status);
CREATE INDEX idx_order_items_order        ON order_items      (order_id);
CREATE INDEX idx_order_items_product      ON order_items      (product_id);
CREATE INDEX idx_users_role               ON users            (role);
