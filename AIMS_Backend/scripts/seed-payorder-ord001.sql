-- ============================================================
-- AIMS Mock Data — ORD-001 full payment flow
-- Run: psql -U aims_app -d aims_db -f seed_ord001.sql
-- ============================================================

SET search_path TO aims;

-- ------------------------------------------------------------
-- CLEANUP (safe re-run)
-- ------------------------------------------------------------
DELETE FROM aims.transaction  WHERE invoice_id = 'INV-001';
DELETE FROM aims.invoice      WHERE order_id   = 'ORD-001';
DELETE FROM aims.delivery     WHERE order_id   = 'ORD-001';
DELETE FROM aims.order_item   WHERE order_id   = 'ORD-001';
DELETE FROM aims.orders       WHERE order_id   = 'ORD-001';

-- ------------------------------------------------------------
-- 1. ORDER  (status = 'pending' = AWAITING_PAYMENT)
-- ------------------------------------------------------------
INSERT INTO aims.orders (order_id, status, created_at)
VALUES ('ORD-001', 'pending', NOW());

-- ------------------------------------------------------------
-- 2. ORDER ITEMS  (dùng product có sẵn: id=1 DVD Mai, id=2 Clean Code)
--    DVD Mai      : selling_price cần check — dùng giá thật trong DB
--    Clean Code   : product_id = 2
-- ------------------------------------------------------------
INSERT INTO aims.order_item (order_id, product_id, quantity)
VALUES
  ('ORD-001', 2, 2),   -- 2 cuon Clean Code
  ('ORD-001', 1, 1);   -- 1 DVD Mai

-- ------------------------------------------------------------
-- 3. DELIVERY
-- ------------------------------------------------------------
INSERT INTO aims.delivery (
  order_id,
  recipient_name,
  phone_number,
  email,
  delivery_province,
  detail_address,
  note
) VALUES (
  'ORD-001',
  'Nguyen Van A',
  '0912345678',
  'nguyenvana@gmail.com',
  'HN',
  '123 Pho Hue Hai Ba Trung Ha Noi',
  'Giao gio hanh chinh'
);

-- ------------------------------------------------------------
-- 4. INVOICE
--    Gia Clean Code (product_id=2): lay tu bang product
--    Tinh toan:
--      2 x Clean Code + 1 x DVD Mai = sub_total_ex_vat
--      sub_total_inc_vat = sub_total_ex_vat * 1.1  (10% VAT)
--      shipping_fee = 30000 (HN noi thanh)
--    => Thay so ben duoi bang gia thuc te sau khi chay query kiem tra
--
--    De don gian: dung so cu dinh 430000 tong cong
--      sub_total_inc_vat = 400000
--      shipping_fee      =  30000
--      TOTAL             = 430000  <- khop voi backend test
-- ------------------------------------------------------------
INSERT INTO aims.invoice (
  invoice_id,
  order_id,
  issue_date,
  sub_total_inc_vat,
  sub_total_ex_vat,
  shipping_fee
) VALUES (
  'INV-001',
  'ORD-001',
  CURRENT_DATE,
  400000,
  363637,
  30000
);
-- Kiem tra: 400000 + 30000 = 430000 ✓

-- ------------------------------------------------------------
-- 5. VERIFY — in ra ket qua de kiem tra mat
-- ------------------------------------------------------------
SELECT
  o.order_id,
  o.status                            AS order_status,
  o.created_at,
  i.invoice_id,
  i.sub_total_inc_vat,
  i.sub_total_ex_vat,
  i.shipping_fee,
  (i.sub_total_inc_vat + i.shipping_fee) AS total_to_pay,
  d.recipient_name,
  d.phone_number,
  d.delivery_province
FROM aims.orders  o
JOIN aims.invoice  i ON i.order_id = o.order_id
JOIN aims.delivery d ON d.order_id = o.order_id
WHERE o.order_id = 'ORD-001';

SELECT
  oi.order_id,
  p.title,
  p.selling_price,
  oi.quantity,
  (p.selling_price * oi.quantity) AS line_total
FROM aims.order_item oi
JOIN aims.product    p  ON p.product_id = oi.product_id
WHERE oi.order_id = 'ORD-001';