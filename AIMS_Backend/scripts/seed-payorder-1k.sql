SET search_path TO aims;

-- Xóa dữ liệu cũ nếu có
DELETE FROM aims.transaction  WHERE invoice_id = 'INV-1K';
DELETE FROM aims.invoice      WHERE order_id   = 'ORD-1K';
DELETE FROM aims.delivery     WHERE order_id   = 'ORD-1K';
DELETE FROM aims.order_item   WHERE order_id   = 'ORD-1K';
DELETE FROM aims.orders       WHERE order_id   = 'ORD-1K';

-- Tạo Order
INSERT INTO aims.orders (order_id, status, created_at)
VALUES ('ORD-1K', 'pending', NOW());

-- Tạo Order Item (dùng product_id = 2 - Clean Code)
INSERT INTO aims.order_item (order_id, product_id, quantity)
VALUES ('ORD-1K', 2, 1);

-- Đảm bảo giá của product_id = 2 là 1000 để khi hiển thị line item khớp với thực tế
UPDATE aims.product SET selling_price = 1000 WHERE product_id = 2;

-- Tạo Delivery
INSERT INTO aims.delivery (
  order_id, recipient_name, phone_number, email, delivery_province, detail_address, note
) VALUES (
  'ORD-1K', 'Khach Test 1K', '0912345678', 'test1k@gmail.com', 'HN', '123 Test', 'Test 1K order'
);

-- Tạo Invoice (Tổng: 1000 VNĐ, Phí ship: 0)
INSERT INTO aims.invoice (
  invoice_id, order_id, issue_date, sub_total_inc_vat, sub_total_ex_vat, shipping_fee
) VALUES (
  'INV-1K', 'ORD-1K', CURRENT_DATE, 1000, 909, 0
);
