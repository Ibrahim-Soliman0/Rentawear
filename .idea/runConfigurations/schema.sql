-- =========================================
-- E-COMMERCE WEB APPLICATION DATABASE
-- Supports: Admin, Users, Clothes Store,
-- Cart Persistence, Orders, Credit Limit
-- =========================================

-- =========================================
-- USERS TABLE
-- =========================================
CREATE TABLE users
(
    id            INT PRIMARY KEY AUTO_INCREMENT,
    name          VARCHAR(100)        NOT NULL,
    email         VARCHAR(150) UNIQUE NOT NULL,
    password_hash VARCHAR(255)        NOT NULL,
    birthday      DATE,
    job           VARCHAR(100),
    address       TEXT,
    credit_limit  DECIMAL(10, 2) DEFAULT 0,
    role          ENUM('ADMIN','USER') DEFAULT 'USER',
    created_at    TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    gender        ENUM('MALE', 'FEMALE'),
    CHECK (credit_limit >= 0)
);


CREATE TABLE payment_cards
(
    id              INT PRIMARY KEY AUTO_INCREMENT,
    card_number     VARCHAR(19)  NOT NULL,
    user_id         INT          NOT NULL,
    cardholder_name VARCHAR(100) NOT NULL,
    card_type       ENUM('VISA', 'MASTERCARD', 'AMERICAN_EXPRESS', 'OTHER') NOT NULL,
    cvv             VARCHAR(4)   NOT NULL,
    expiry_month    CHAR(2)      NOT NULL,
    expiry_year     CHAR(4)      NOT NULL,
    UNIQUE (user_id, card_number),
    FOREIGN KEY (user_id) REFERENCES users (id)
);

-- =========================================
-- CATEGORIES
-- =========================================
CREATE TABLE categories
(
    id          INT PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(100) NOT NULL,
    description TEXT,
    gender      ENUM('MALE', 'FEMALE') NOT NULL
);


CREATE TABLE user_category
(
    id          INT PRIMARY KEY AUTO_INCREMENT,
    category_id INT,
    user_id     INT,
    UNIQUE (user_id, category_id),
    FOREIGN KEY (category_id) REFERENCES categories (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);

-- =========================================
-- PRODUCTS
-- =========================================
CREATE TABLE products
(
    id          INT PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(150)   NOT NULL,
    description TEXT,
    base_price  DECIMAL(10, 2) NOT NULL,
    category_id INT,
    image_url   VARCHAR(255),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted     BOOLEAN DEFAULT FALSE NOT NULL,
    FOREIGN KEY (category_id) REFERENCES categories (id)
);
CREATE INDEX idx_products_deleted ON products  (deleted);


-- =========================================
-- PRODUCT VARIANTS (Size / Color / Stock)
-- =========================================
CREATE TABLE product_variants
(
    id         INT PRIMARY KEY AUTO_INCREMENT,
    product_id INT         NOT NULL,
    size       VARCHAR(20),
    color      VARCHAR(50) NOT NULL,
    quantity   INT         NOT NULL,
    version    BIGINT      NOT NULL,
    UNIQUE (product_id, color, size),
    CHECK  (quantity >= 0),
    deleted    BOOLEAN DEFAULT FALSE NOT NULL,
    FOREIGN KEY (product_id) REFERENCES products (id)
);
CREATE INDEX idx_product_variants_deleted ON product_variants (deleted);
-- =========================================
-- PRODUCT IMAGES (Optional)
-- =========================================
CREATE TABLE product_images
(
    id         INT PRIMARY KEY AUTO_INCREMENT,
    product_id INT,
    image_url  VARCHAR(255),
    color      VARCHAR(50),
    UNIQUE (product_id, color, image_url),
    FOREIGN KEY (product_id) REFERENCES products (id)
);

-- =========================================
-- SHOPPING CART (One per user)
-- =========================================
CREATE TABLE carts
(
    id         INT PRIMARY KEY AUTO_INCREMENT,
    user_id    INT UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id)
);

-- =========================================
-- CART ITEMS
-- =========================================
CREATE TABLE cart_items
(
    id         INT PRIMARY KEY AUTO_INCREMENT,
    cart_id    INT,
    variant_id INT,
    quantity   INT  NOT NULL,
    start_date DATE NOT NULL,
    end_date   DATE NOT NULL,
    FOREIGN KEY (cart_id) REFERENCES carts (id),
    FOREIGN KEY (variant_id) REFERENCES product_variants (id)
);

-- =========================================
-- ORDERS
-- =========================================
CREATE TABLE orders
(
    id           INT PRIMARY KEY AUTO_INCREMENT,
    user_id      INT,
    total_amount DECIMAL(10, 2),
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status       ENUM('ORDERED', 'CONFIRMED', 'CANCELLED', 'SHIPPED', 'DELIVERED', 'RETURNED') DEFAULT 'ORDERED',
    FOREIGN KEY (user_id) REFERENCES users (id)
);

-- =========================================
-- ORDER ITEMS
-- =========================================
CREATE TABLE order_items
(
    id                INT PRIMARY KEY AUTO_INCREMENT,
    order_id          INT,
    variant_id        INT,
    quantity          INT,
    price_at_purchase DECIMAL(10, 2),
    start_date        DATE NOT NULL,
    end_date          DATE NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders (id),
    FOREIGN KEY (variant_id) REFERENCES product_variants (id)
);
