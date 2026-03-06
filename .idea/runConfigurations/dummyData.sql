-- =========================================
-- USERS
-- =========================================

INSERT INTO users (id, name, email, password_hash, birthday, job, address, interests, credit_limit, role)
VALUES
(1, 'System Admin', 'admin@shop.com', 'admin_hash', '1990-01-01', 'Administrator', 'Cairo', 'Management', 0, 'ADMIN'),
(2, 'Ahmed Ali', 'ahmed@gmail.com', 'hash1', '1998-05-10', 'Engineer', 'Nasr City', 'Sports', 5000, 'USER'),
(3, 'Sara Mohamed', 'sara@gmail.com', 'hash2', '1999-07-20', 'Designer', 'Maadi', 'Fashion', 3000, 'USER'),
(4, 'Omar Hassan', 'omar@gmail.com', 'hash3', '1995-03-15', 'Teacher', 'Giza', 'Reading', 1500, 'USER');

-- =========================================
-- CATEGORIES
-- =========================================

INSERT INTO categories (id, name, description) VALUES
(1, 'Men', 'Men clothing'),
(2, 'Women', 'Women clothing'),
(3, 'Kids', 'Kids clothing');

-- =========================================
-- PRODUCTS
-- =========================================

INSERT INTO products (id, name, description, base_price, category_id, image_url) VALUES
(1, 'Basic T-Shirt', 'Cotton T-shirt', 200, 1, 'tshirt.jpg'),
(2, 'Formal Shirt', 'Slim fit formal shirt', 450, 1, 'shirt.jpg'),
(3, 'Summer Dress', 'Light summer dress', 600, 2, 'dress.jpg'),
(4, 'Kids Hoodie', 'Warm hoodie for kids', 350, 3, 'hoodie.jpg');

-- =========================================
-- PRODUCT VARIANTS (Size / Color / Stock)
-- =========================================

INSERT INTO product_variants (id, product_id, size, color, quantity, price) VALUES
(1, 1, 'M', 'Black', 50, 200),
(2, 1, 'L', 'Black', 40, 200),
(3, 1, 'M', 'White', 30, 210),

(4, 2, 'M', 'Blue', 25, 450),
(5, 2, 'L', 'Blue', 20, 450),

(6, 3, 'S', 'Red', 15, 600),
(7, 3, 'M', 'Red', 10, 600),

(8, 4, 'XS', 'Green', 18, 350),
(9, 4, 'S', 'Green', 12, 350);

-- =========================================
-- PRODUCT IMAGES
-- =========================================

INSERT INTO product_images (product_id, image_url) VALUES
(1, 'tshirt1.jpg'),
(1, 'tshirt2.jpg'),
(2, 'shirt1.jpg'),
(3, 'dress1.jpg'),
(4, 'hoodie1.jpg');

-- =========================================
-- CARTS (Users who have not checked out)
-- =========================================

INSERT INTO carts (id, user_id) VALUES
(1, 2),
(2, 3);

-- =========================================
-- CART ITEMS
-- =========================================

INSERT INTO cart_items (cart_id, variant_id, quantity) VALUES
(1, 1, 2),   -- Ahmed: 2 Black T-Shirts M
(1, 4, 1),   -- Ahmed: 1 Formal Shirt M
(2, 6, 1);   -- Sara: 1 Summer Dress S

-- =========================================
-- ORDERS (Completed purchases)
-- =========================================

INSERT INTO orders (id, user_id, total_amount) VALUES
(1, 4, 700),   -- Omar order
(2, 2, 450);   -- Ahmed previous order

-- =========================================
-- ORDER ITEMS
-- =========================================

INSERT INTO order_items (order_id, variant_id, quantity, price_at_purchase) VALUES
(1, 8, 2, 350),  -- Omar bought 2 Kids Hoodies XS
(2, 5, 1, 450);  -- Ahmed bought 1 Formal Shirt L