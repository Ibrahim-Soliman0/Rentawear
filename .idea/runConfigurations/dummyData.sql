-- =========================================
-- USERS
-- =========================================

INSERT INTO users (id, name, email, password_hash, birthday, job, address, interests, credit_limit, role, gender)
VALUES (1, 'System Admin', 'admin@shop.com', 'admin_hash', '1990-01-01', 'Administrator', 'Cairo', 'Management', 0,
        'ADMIN', 'MALE'),
       (2, 'Ahmed Ali', 'ahmed@gmail.com', 'hash1', '1998-05-10', 'Engineer', 'Nasr City', 'Sports', 5000, 'USER',
        'MALE'),
       (3, 'Sara Mohamed', 'sara@gmail.com', 'hash2', '1999-07-20', 'Designer', 'Maadi', 'Fashion', 3000, 'USER',
        'FEMALE'),
       (4, 'Omar Hassan', 'omar@gmail.com', 'hash3', '1995-03-15', 'Teacher', 'Giza', 'Reading', 1500, 'USER', 'MALE');
-- =========================================
-- CATEGORIES
-- =========================================

INSERT INTO payment_cards
(card_number, user_id, cardholder_name, card_type, cvv, expiry_month, expiry_year)
VALUES ('4111111111111111', 2, 'Ahmed Ali', 'VISA', '123', '05', '2028'),
       ('5500000000000004', 3, 'Sara Mohamed', 'MASTERCARD', '456', '09', '2027'),
       ('340000000000009', 4, 'Omar Hassan', 'AMERICAN_EXPRESS', '1234', '12', '2029');

INSERT INTO categories (id, name, description, gender)
VALUES (1, 'Men', 'Men clothing', 'MALE'),
       (2, 'Women', 'Women clothing', 'FEMALE'),
       (3, 'Kids', 'Kids clothing', 'MALE');


INSERT INTO user_category (category_id, user_id)
VALUES (1, 2),
       (2, 3),
       (3, 4);

-- =========================================
-- PRODUCTS
-- =========================================

INSERT INTO products (id, name, description, base_price, category_id, image_url)
VALUES (1, 'Basic T-Shirt', 'Cotton T-shirt', 200, 1, 'tshirt.jpg'),
       (2, 'Formal Shirt', 'Slim fit formal shirt', 450, 1, 'shirt.jpg'),
       (3, 'Summer Dress', 'Light summer dress', 600, 2, 'dress.jpg'),
       (4, 'Kids Hoodie', 'Warm hoodie for kids', 350, 3, 'hoodie.jpg');

-- =========================================
-- PRODUCT VARIANTS (Size / Color / Stock)
-- =========================================

INSERT INTO product_variants (id, product_id, size, color, quantity)
VALUES (1, 1, 'M', 'Black', 50),
       (2, 1, 'L', 'Black', 40),
       (3, 1, 'M', 'White', 30),

       (4, 2, 'M', 'Blue', 25),
       (5, 2, 'L', 'Blue', 20),

       (6, 3, 'S', 'Red', 15),
       (7, 3, 'M', 'Red', 10),

       (8, 4, 'XS', 'Green', 18),
       (9, 4, 'S', 'Green', 12);

-- =========================================
-- PRODUCT IMAGES
-- =========================================

INSERT INTO product_images (product_id, image_url, color)
VALUES (1, 'tshirt_black_1.jpg', 'Black'),
       (1, 'tshirt_black_2.jpg', 'Black'),
       (1, 'tshirt_white_1.jpg', 'White'),

       (2, 'shirt_blue_1.jpg', 'Blue'),

       (3, 'dress_red_1.jpg', 'Red'),

       (4, 'hoodie_green_1.jpg', 'Green');

-- =========================================
-- CARTS (Users who have not checked out)
-- =========================================

INSERT INTO carts (id, user_id)
VALUES (1, 2),
       (2, 3);

-- =========================================
-- CART ITEMS
-- =========================================

INSERT INTO cart_items (cart_id, variant_id, quantity, start_date, end_date)
VALUES (1, 1, 2, '2026-04-01', '2026-04-05'),
       (1, 4, 1, '2026-04-01', '2026-04-03'),
       (2, 6, 1, '2026-05-10', '2026-05-15');

-- =========================================
-- ORDERS (Completed purchases)
-- =========================================

INSERT INTO orders (id, user_id, total_amount, status)
VALUES (1, 4, 700, 'DELIVERED'),
       (2, 2, 450, 'CONFIRMED');

-- =========================================
-- ORDER ITEMS
-- =========================================

INSERT INTO order_items (order_id, variant_id, quantity, price_at_purchase, start_date, end_date)
VALUES (1, 8, 2, 350, '2026-03-01', '2026-03-07'),
       (2, 5, 1, 450, '2026-02-10', '2026-02-15');