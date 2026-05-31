SET search_path TO aims;

-- 1. Reset order status
UPDATE aims.orders SET status = 'pending' WHERE order_id = 'ORD-001';

-- 2. Delete any existing transactions for this invoice so VietQR can generate a new one
DELETE FROM aims.transaction WHERE invoice_id = 'INV-001';

-- 3. Update invoice to be 1000 VND total (sub_total_inc_vat = 1000, shipping_fee = 0)
UPDATE aims.invoice 
SET sub_total_inc_vat = 1000,
    sub_total_ex_vat = 909,
    shipping_fee = 0
WHERE invoice_id = 'INV-001';

-- 4. Update the order_item so it looks consistent (optional but good)
-- Clean Code price -> 1000
UPDATE aims.product SET selling_price = 1000 WHERE product_id = 2;
-- Remove DVD Mai from order_item just to keep it simple, or leave it. 
-- Let's just leave it and set its quantity to 0 or delete it.
DELETE FROM aims.order_item WHERE order_id = 'ORD-001' AND product_id = 1;
UPDATE aims.order_item SET quantity = 1 WHERE order_id = 'ORD-001' AND product_id = 2;

