-- =========================================
-- RENTAWEAR — TEST DATA
-- Run this after your schema is created.
-- Covers all servlet scenarios:
--   list, filter by gender, filter by category,
--   price filter, new arrivals, search, images.
-- =========================================

-- =========================================
-- CATEGORIES
-- 3 female, 2 male — enough to test gender
-- filtering and category ID filtering together
-- =========================================
INSERT INTO categories (id, name, description, gender) VALUES
                                                           (1,  'Dresses',          'Evening, midi, mini and occasion dresses',     'FEMALE'),
                                                           (2,  'Jumpsuits',        'One-piece jumpsuits and playsuits',             'FEMALE'),
                                                           (3,  'Accessories',      'Bags, jewellery, wraps and coverups',           'FEMALE'),
                                                           (4,  'Suits',            'Tuxedos, slim-fit and classic suits',           'MALE'),
                                                           (5,  'Shirts & Tops',    'Formal and casual shirts for men',              'MALE');


-- =========================================
-- USERS
-- Passwords are bcrypt hashes of "Password1!"
-- =========================================
INSERT INTO users (id, name, email, password_hash, birthday, job, address, credit_limit, role, gender) VALUES
                                                                                                           (1, 'Admin User',
                                                                                                            'admin@rentawear.com',
                                                                                                            '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
                                                                                                            '1985-03-12', 'Administrator', '1 Admin Lane, London, EC1A 1BB',
                                                                                                            0.00, 'ADMIN', 'FEMALE'),

                                                                                                           (2, 'Emma Clarke',
                                                                                                            'emma@example.com',
                                                                                                            '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
                                                                                                            '1995-07-22', 'Marketing Manager', '14 Bloom Street, London, W1B 4DG',
                                                                                                            500.00, 'USER', 'FEMALE'),

                                                                                                           (3, 'James Harlow',
                                                                                                            'james@example.com',
                                                                                                            '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
                                                                                                            '1990-11-05', 'Lawyer', '9 Oak Road, Manchester, M1 2AA',
                                                                                                            300.00, 'USER', 'MALE');


-- =========================================
-- USER INTERESTS
-- Emma is interested in Dresses and Accessories.
-- James is interested in Suits.
-- Used to test action=interests.
-- =========================================
INSERT INTO user_category (category_id, user_id) VALUES
                                                     (1, 2),
                                                     (3, 2),
                                                     (4, 3);


-- =========================================
-- PRODUCTS
-- Mix of price points, categories, genders.
-- created_at spread so new arrivals tests work:
--   products 1-4 → within last 30 days (show in new arrivals)
--   products 5-7 → older (do NOT show in new arrivals)
-- image_url is null for all — upload servlet sets this.
-- A placeholder will be shown until images are uploaded.
-- =========================================
INSERT INTO products (id, name, description, base_price, category_id, image_url, created_at) VALUES

-- ── Women's Dresses (category 1) ─────────────────────────────────
(1, 'Luna Bias-Cut Gown',
 'A fluid, floor-length bias-cut gown in duchess satin. Features a subtle cowl neckline and open back. Perfect for black-tie and formal occasions.',
 85.00, 1, NULL,
 NOW()),

(2, 'Celeste Midi Dress',
 'An elegant midi dress with a tiered skirt and delicate floral embroidery. Fully lined with a concealed zip. Ideal for weddings and garden parties.',
 55.00, 1, NULL,
 DATE_SUB(NOW(), INTERVAL 7 DAY)),

(3, 'Harper Wrap Dress',
 'A classic wrap dress in a fluid jersey. Universally flattering silhouette with a deep V-neckline and adjustable tie. Effortlessly elegant for cocktail events.',
 40.00, 1, NULL,
 DATE_SUB(NOW(), INTERVAL 14 DAY)),

-- ── Women's Jumpsuits (category 2) ───────────────────────────────
(4, 'Riviera Wide-Leg Jumpsuit',
 'A tailored wide-leg jumpsuit in crepe with a statement belt. Features a halter neck and side pockets. Great for cocktail events and business occasions.',
 60.00, 2, NULL,
 DATE_SUB(NOW(), INTERVAL 20 DAY)),

-- ── Women's Accessories (category 3) ─────────────────────────────
(5, 'Aurum Evening Clutch',
 'A structured minaudière in gold-plated brass mesh. Magnetic clasp closure with a detachable chain strap. Fits phone, cards and lipstick.',
 25.00, 3, NULL,
 DATE_SUB(NOW(), INTERVAL 45 DAY)),

-- ── Men's Suits (category 4) ─────────────────────────────────────
(6, 'Regent Tuxedo',
 'A peak-lapel tuxedo in wool-silk blend. Includes matching trousers with satin side stripe. Dry-cleaned and pressed before each rental.',
 95.00, 4, NULL,
 DATE_SUB(NOW(), INTERVAL 60 DAY)),

(7, 'Chester Slim-Fit Suit',
 'A slim-fit two-piece suit in pure wool. Notch lapels with a two-button front. Available in three classic colourways. Dry-cleaned and pressed.',
 70.00, 4, NULL,
 DATE_SUB(NOW(), INTERVAL 90 DAY));


-- =========================================
-- PRODUCT VARIANTS
-- color column MUST be in format: #hex-Display Name
-- This is what ColorUtil parses.
-- Each product has 2-3 colors × multiple sizes.
-- =========================================

-- ── Product 1: Luna Bias-Cut Gown ─────────────────────────────────
-- Colors: Midnight Navy, Champagne
INSERT INTO product_variants (product_id, size, color, quantity) VALUES
                                                                     (1, 'XS', '#1B2A4A-Midnight Navy',  3),
                                                                     (1, 'S',  '#1B2A4A-Midnight Navy',  4),
                                                                     (1, 'M',  '#1B2A4A-Midnight Navy',  3),
                                                                     (1, 'L',  '#1B2A4A-Midnight Navy',  2),
                                                                     (1, 'XS', '#F5E6C8-Champagne',      2),
                                                                     (1, 'S',  '#F5E6C8-Champagne',      4),
                                                                     (1, 'M',  '#F5E6C8-Champagne',      3),
                                                                     (1, 'L',  '#F5E6C8-Champagne',      2);

-- ── Product 2: Celeste Midi Dress ─────────────────────────────────
-- Colors: Blush Pink, Sage Green, Ivory
INSERT INTO product_variants (product_id, size, color, quantity) VALUES
                                                                     (2, 'XS', '#F4C2C2-Blush Pink',     3),
                                                                     (2, 'S',  '#F4C2C2-Blush Pink',     5),
                                                                     (2, 'M',  '#F4C2C2-Blush Pink',     4),
                                                                     (2, 'L',  '#F4C2C2-Blush Pink',     2),
                                                                     (2, 'XL', '#F4C2C2-Blush Pink',     1),
                                                                     (2, 'XS', '#8FAF8F-Sage Green',     2),
                                                                     (2, 'S',  '#8FAF8F-Sage Green',     3),
                                                                     (2, 'M',  '#8FAF8F-Sage Green',     4),
                                                                     (2, 'L',  '#8FAF8F-Sage Green',     2),
                                                                     (2, 'XS', '#FFFFF0-Ivory',          2),
                                                                     (2, 'S',  '#FFFFF0-Ivory',          3),
                                                                     (2, 'M',  '#FFFFF0-Ivory',          3);

-- ── Product 3: Harper Wrap Dress ──────────────────────────────────
-- Colors: Burgundy, Midnight Navy
INSERT INTO product_variants (product_id, size, color, quantity) VALUES
                                                                     (3, 'XS', '#800020-Burgundy',       2),
                                                                     (3, 'S',  '#800020-Burgundy',       4),
                                                                     (3, 'M',  '#800020-Burgundy',       5),
                                                                     (3, 'L',  '#800020-Burgundy',       3),
                                                                     (3, 'XL', '#800020-Burgundy',       2),
                                                                     (3, 'XS', '#1B2A4A-Midnight Navy',  3),
                                                                     (3, 'S',  '#1B2A4A-Midnight Navy',  4),
                                                                     (3, 'M',  '#1B2A4A-Midnight Navy',  3),
                                                                     (3, 'L',  '#1B2A4A-Midnight Navy',  2);

-- ── Product 4: Riviera Wide-Leg Jumpsuit ──────────────────────────
-- Colors: Camel, Onyx Black
INSERT INTO product_variants (product_id, size, color, quantity) VALUES
                                                                     (4, 'XS', '#C19A6B-Camel',          2),
                                                                     (4, 'S',  '#C19A6B-Camel',          3),
                                                                     (4, 'M',  '#C19A6B-Camel',          4),
                                                                     (4, 'L',  '#C19A6B-Camel',          2),
                                                                     (4, 'XS', '#1C1C1C-Onyx Black',     3),
                                                                     (4, 'S',  '#1C1C1C-Onyx Black',     4),
                                                                     (4, 'M',  '#1C1C1C-Onyx Black',     5),
                                                                     (4, 'L',  '#1C1C1C-Onyx Black',     3),
                                                                     (4, 'XL', '#1C1C1C-Onyx Black',     2);

-- ── Product 5: Aurum Evening Clutch ───────────────────────────────
-- One size, two colorways
INSERT INTO product_variants (product_id, size, color, quantity) VALUES
                                                                     (5, 'One Size', '#D4AF37-Gold',      6),
                                                                     (5, 'One Size', '#C0C0C0-Silver',    6);

-- ── Product 6: Regent Tuxedo ──────────────────────────────────────
-- Colors: Onyx Black, Midnight Navy
INSERT INTO product_variants (product_id, size, color, quantity) VALUES
                                                                     (6, 'S',   '#1C1C1C-Onyx Black',    3),
                                                                     (6, 'M',   '#1C1C1C-Onyx Black',    4),
                                                                     (6, 'L',   '#1C1C1C-Onyx Black',    4),
                                                                     (6, 'XL',  '#1C1C1C-Onyx Black',    2),
                                                                     (6, 'S',   '#1B2A4A-Midnight Navy', 2),
                                                                     (6, 'M',   '#1B2A4A-Midnight Navy', 3),
                                                                     (6, 'L',   '#1B2A4A-Midnight Navy', 3),
                                                                     (6, 'XL',  '#1B2A4A-Midnight Navy', 2);

-- ── Product 7: Chester Slim-Fit Suit ──────────────────────────────
-- Colors: Charcoal, Stone Grey, Onyx Black
INSERT INTO product_variants (product_id, size, color, quantity) VALUES
                                                                     (7, 'S',  '#36454F-Charcoal',       3),
                                                                     (7, 'M',  '#36454F-Charcoal',       5),
                                                                     (7, 'L',  '#36454F-Charcoal',       4),
                                                                     (7, 'XL', '#36454F-Charcoal',       2),
                                                                     (7, 'S',  '#8C8C8C-Stone Grey',     3),
                                                                     (7, 'M',  '#8C8C8C-Stone Grey',     4),
                                                                     (7, 'L',  '#8C8C8C-Stone Grey',     3),
                                                                     (7, 'S',  '#1C1C1C-Onyx Black',     2),
                                                                     (7, 'M',  '#1C1C1C-Onyx Black',     4),
                                                                     (7, 'L',  '#1C1C1C-Onyx Black',     3),
                                                                     (7, 'XL', '#1C1C1C-Onyx Black',     2);


-- =========================================
-- PRODUCT IMAGES
-- image_url is the base path stored by the
-- upload servlet (no size suffix, no extension).
-- These are placeholder paths — replace with
-- real uploads via ImageUploadServlet.
-- color MUST exactly match the variant color.
-- =========================================

INSERT INTO product_images (product_id, image_url, color) VALUES
-- Product 1 — Luna Bias-Cut Gown
(1, '/assets/img/products/1/midnight-navy/placeholder', '#1B2A4A-Midnight Navy'),
(1, '/assets/img/products/1/champagne/placeholder',     '#F5E6C8-Champagne'),

-- Product 2 — Celeste Midi Dress
(2, '/assets/img/products/2/blush-pink/placeholder',    '#F4C2C2-Blush Pink'),
(2, '/assets/img/products/2/sage-green/placeholder',    '#8FAF8F-Sage Green'),
(2, '/assets/img/products/2/ivory/placeholder',         '#FFFFF0-Ivory'),

-- Product 3 — Harper Wrap Dress
(3, '/assets/img/products/3/burgundy/placeholder',      '#800020-Burgundy'),
(3, '/assets/img/products/3/midnight-navy/placeholder', '#1B2A4A-Midnight Navy'),

-- Product 4 — Riviera Wide-Leg Jumpsuit
(4, '/assets/img/products/4/camel/placeholder',         '#C19A6B-Camel'),
(4, '/assets/img/products/4/onyx-black/placeholder',    '#1C1C1C-Onyx Black'),

-- Product 5 — Aurum Evening Clutch
(5, '/assets/img/products/5/gold/placeholder',          '#D4AF37-Gold'),
(5, '/assets/img/products/5/silver/placeholder',        '#C0C0C0-Silver'),

-- Product 6 — Regent Tuxedo
(6, '/assets/img/products/6/onyx-black/placeholder',    '#1C1C1C-Onyx Black'),
(6, '/assets/img/products/6/midnight-navy/placeholder', '#1B2A4A-Midnight Navy'),

-- Product 7 — Chester Slim-Fit Suit
(7, '/assets/img/products/7/charcoal/placeholder',      '#36454F-Charcoal'),
(7, '/assets/img/products/7/stone-grey/placeholder',    '#8C8C8C-Stone Grey'),
(7, '/assets/img/products/7/onyx-black/placeholder',    '#1C1C1C-Onyx Black');


-- Update Product.imageUrl to the primary image base path
-- (lowest id image for the default color = lowest id variant's color)
UPDATE products SET image_url = '/assets/img/products/1/midnight-navy/placeholder' WHERE id = 1;
UPDATE products SET image_url = '/assets/img/products/2/blush-pink/placeholder'    WHERE id = 2;
UPDATE products SET image_url = '/assets/img/products/3/burgundy/placeholder'      WHERE id = 3;
UPDATE products SET image_url = '/assets/img/products/4/camel/placeholder'         WHERE id = 4;
UPDATE products SET image_url = '/assets/img/products/5/gold/placeholder'          WHERE id = 5;
UPDATE products SET image_url = '/assets/img/products/6/onyx-black/placeholder'    WHERE id = 6;
UPDATE products SET image_url = '/assets/img/products/7/charcoal/placeholder'      WHERE id = 7;


-- =========================================
-- CARTS
-- One cart per user (Emma and James)
-- =========================================
INSERT INTO carts (id, user_id) VALUES
                                    (1, 2),
                                    (2, 3);


-- =========================================
-- CART ITEMS
-- Emma has two items in her cart.
-- James has one item.
-- Dates are upcoming so they are valid rentals.
-- variant_id references must match the
-- product_variants rows inserted above.
-- =========================================

-- Emma's cart (cart_id = 1)
-- Luna Bias-Cut Gown, size S, Midnight Navy
INSERT INTO cart_items (cart_id, variant_id, quantity, start_date, end_date)
SELECT 1, id, 1,
       DATE_ADD(CURDATE(), INTERVAL 14 DAY),
       DATE_ADD(CURDATE(), INTERVAL 17 DAY)
FROM product_variants
WHERE product_id = 1 AND color = '#1B2A4A-Midnight Navy' AND size = 'S';

-- Celeste Midi Dress, size M, Blush Pink
INSERT INTO cart_items (cart_id, variant_id, quantity, start_date, end_date)
SELECT 1, id, 1,
       DATE_ADD(CURDATE(), INTERVAL 14 DAY),
       DATE_ADD(CURDATE(), INTERVAL 17 DAY)
FROM product_variants
WHERE product_id = 2 AND color = '#F4C2C2-Blush Pink' AND size = 'M';

-- James's cart (cart_id = 2)
-- Regent Tuxedo, size L, Onyx Black
INSERT INTO cart_items (cart_id, variant_id, quantity, start_date, end_date)
SELECT 2, id, 1,
       DATE_ADD(CURDATE(), INTERVAL 7 DAY),
       DATE_ADD(CURDATE(), INTERVAL 10 DAY)
FROM product_variants
WHERE product_id = 6 AND color = '#1C1C1C-Onyx Black' AND size = 'L';


-- =========================================
-- VERIFICATION QUERIES
-- Run these after inserting to confirm
-- everything looks right before testing
-- the servlet.
-- =========================================

-- Should return 7 products
SELECT id, name, base_price, category_id, image_url FROM products ORDER BY id;

-- Should show encoded colors in #hex-Name format
SELECT product_id, color, size, quantity FROM product_variants ORDER BY product_id, id;

-- Should return products 1-4 only (within last 30 days)
SELECT id, name, created_at FROM products
WHERE created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
ORDER BY created_at DESC;

-- Should return only female products (ids 1-5)
SELECT p.id, p.name, c.gender
FROM products p
         JOIN categories c ON c.id = p.category_id
WHERE c.gender = 'FEMALE';

-- Should return only male products (ids 6-7)
SELECT p.id, p.name, c.gender
FROM products p
         JOIN categories c ON c.id = p.category_id
WHERE c.gender = 'MALE';

-- Should return products 1-4 (price 40-85, female)
SELECT p.id, p.name, p.base_price
FROM products p
         JOIN categories c ON c.id = p.category_id
WHERE c.gender = 'FEMALE'
  AND p.base_price BETWEEN 40 AND 85
ORDER BY p.base_price;

-- Color format check — all should start with # and contain a hyphen
SELECT DISTINCT color,
                CASE WHEN color REGEXP '^#[0-9A-Fa-f]{6}-.+' THEN 'OK' ELSE 'BAD FORMAT' END AS format_check
FROM product_variants;

-- =========================================
-- ORDERS
-- =========================================

-- Emma (user_id = 2)
INSERT INTO orders (id, user_id, total_amount, status, created_at) VALUES
                                                                       (1, 2, 140.00, 'DELIVERED', DATE_SUB(NOW(), INTERVAL 10 DAY)),
                                                                       (2, 2,  55.00, 'CANCELLED', DATE_SUB(NOW(), INTERVAL 3 DAY));

-- James (user_id = 3)
INSERT INTO orders (id, user_id, total_amount, status, created_at) VALUES
    (3, 3, 95.00, 'SHIPPED', DATE_SUB(NOW(), INTERVAL 2 DAY));

-- Luna Gown
INSERT INTO order_items (order_id, variant_id, quantity, price_at_purchase, start_date, end_date)
SELECT 1, id, 1, 85.00,
       DATE_SUB(CURDATE(), INTERVAL 15 DAY),
       DATE_SUB(CURDATE(), INTERVAL 12 DAY)
FROM product_variants
WHERE product_id = 1 AND color = '#1B2A4A-Midnight Navy' AND size = 'S';

-- Celeste Dress
INSERT INTO order_items (order_id, variant_id, quantity, price_at_purchase, start_date, end_date)
SELECT 1, id, 1, 55.00,
       DATE_SUB(CURDATE(), INTERVAL 15 DAY),
       DATE_SUB(CURDATE(), INTERVAL 12 DAY)
FROM product_variants
WHERE product_id = 2 AND color = '#F4C2C2-Blush Pink' AND size = 'M';

INSERT INTO order_items (order_id, variant_id, quantity, price_at_purchase, start_date, end_date)
SELECT 2, id, 1, 55.00,
       DATE_ADD(CURDATE(), INTERVAL 5 DAY),
       DATE_ADD(CURDATE(), INTERVAL 8 DAY)
FROM product_variants
WHERE product_id = 2 AND color = '#8FAF8F-Sage Green' AND size = 'S';

INSERT INTO order_items (order_id, variant_id, quantity, price_at_purchase, start_date, end_date)
SELECT 3, id, 1, 95.00,
       DATE_ADD(CURDATE(), INTERVAL 3 DAY),
       DATE_ADD(CURDATE(), INTERVAL 6 DAY)
FROM product_variants
WHERE product_id = 6 AND color = '#1C1C1C-Onyx Black' AND size = 'L';