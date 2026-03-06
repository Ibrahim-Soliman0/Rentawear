-- =========================================
-- E-COMMERCE WEB APPLICATION DATABASE
-- Supports: Admin, Users, Clothes Store,
-- Cart Persistence, Orders, Credit Limit
-- =========================================

-- =========================================
-- USERS TABLE
-- =========================================
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    birthday DATE,
    job VARCHAR(100),
    address TEXT,
    interests TEXT,
    credit_limit DECIMAL(10,2) DEFAULT 0,
    role ENUM('ADMIN','USER') DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =========================================
-- CATEGORIES
-- =========================================
CREATE TABLE categories (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT
);

-- =========================================
-- PRODUCTS
-- =========================================
CREATE TABLE products (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    base_price DECIMAL(10,2) NOT NULL,
    category_id INT,
    image_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- =========================================
-- PRODUCT VARIANTS (Size / Color / Stock)
-- =========================================
CREATE TABLE product_variants (
    id INT PRIMARY KEY AUTO_INCREMENT,
    product_id INT NOT NULL,
    size VARCHAR(20),
    color VARCHAR(50),
    quantity INT NOT NULL,
    price DECIMAL(10,2),
    FOREIGN KEY (product_id) REFERENCES products(id)
);

-- =========================================
-- PRODUCT IMAGES (Optional)
-- =========================================
CREATE TABLE product_images (
    id INT PRIMARY KEY AUTO_INCREMENT,
    product_id INT,
    image_url VARCHAR(255),
    FOREIGN KEY (product_id) REFERENCES products(id)
);

-- =========================================
-- SHOPPING CART (One per user)
-- =========================================
CREATE TABLE carts (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- =========================================
-- CART ITEMS
-- =========================================
CREATE TABLE cart_items (
    id INT PRIMARY KEY AUTO_INCREMENT,
    cart_id INT,
    variant_id INT,
    quantity INT NOT NULL,
    FOREIGN KEY (cart_id) REFERENCES carts(id),
    FOREIGN KEY (variant_id) REFERENCES product_variants(id)
);

-- =========================================
-- ORDERS
-- =========================================
CREATE TABLE orders (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    total_amount DECIMAL(10,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- =========================================
-- ORDER ITEMS
-- =========================================
CREATE TABLE order_items (
    id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT,
    variant_id INT,
    quantity INT,
    price_at_purchase DECIMAL(10,2),
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (variant_id) REFERENCES product_variants(id)
);
