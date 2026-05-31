-- ============================================================
-- AIMS Mock Data — ORD-002 full payment flow
-- ============================================================

SET search_path TO aims;

-- CLEANUP (safe re-run)
DELETE FROM aims.transaction  WHERE invoice_id = 'INV-002';
DELETE FROM aims.invoice      WHERE order_id   = 'ORD-002';
DELETE FROM aims.delivery     WHERE order_id   = 'ORD-002';
DELETE FROM aims.order_item   WHERE order_id   = 'ORD-002';
DELETE FROM aims.orders       WHERE order_id   = 'ORD-002';

-- 1. ORDER (status = 'pending' = AWAITING_PAYMENT)
INSERT INTO aims.orders (order_id, status, created_at)
VALUES ('ORD-002', 'pending', NOW());

-- 2. ORDER ITEMS
INSERT INTO aims.order_item (order_id, product_id, quantity)
VALUES
  ('ORD-002', 2, 1); -- 1 Clean Code

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
  'ORD-002',
  'Tran Van B',
  '0987654321',
  'tranvanb@gmail.com',
  'HCM',
  '456 Le Loi District 1 HCMC',
  'Giao nhanh'
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
  'INV-002',
  'ORD-002',
  CURRENT_DATE,
  10000,
  9091,
  2000
);
-- Total amount: 10000 + 2000 = 12000 VND
