-- ============================================================
-- AIMS Mock Data — ORD-003 full payment flow
-- ============================================================

SET search_path TO aims;

-- CLEANUP (safe re-run)
DELETE FROM aims.transaction  WHERE invoice_id = 'INV-003';
DELETE FROM aims.invoice      WHERE order_id   = 'ORD-003';
DELETE FROM aims.delivery     WHERE order_id   = 'ORD-003';
DELETE FROM aims.order_item   WHERE order_id   = 'ORD-003';
DELETE FROM aims.orders       WHERE order_id   = 'ORD-003';

-- 1. ORDER (status = 'pending' = AWAITING_PAYMENT)
INSERT INTO aims.orders (order_id, status, created_at)
VALUES ('ORD-003', 'pending', NOW());

-- 2. ORDER ITEMS
INSERT INTO aims.order_item (order_id, product_id, quantity)
VALUES
  ('ORD-003', 2, 3); -- 3 Clean Code

-- 3. DELIVERY
INSERT INTO aims.delivery (
  order_id,
  recipient_name,
  phone_number,
  email,
  delivery_province,
  detail_address,
  note
) VALUES (
  'ORD-003',
  'Le Thi C',
  '0901234567',
  'lethic@gmail.com',
  'DN',
  '789 Nguyen Van Linh Da Nang',
  'Giao gap'
);

-- 4. INVOICE
INSERT INTO aims.invoice (
  invoice_id,
  order_id,
  issue_date,
  sub_total_inc_vat,
  sub_total_ex_vat,
  shipping_fee
) VALUES (
  'INV-003',
  'ORD-003',
  CURRENT_DATE,
  15000,
  13636,
  5000
);
-- Total amount: 15000 + 5000 = 20000 VND
