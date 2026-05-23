package com.hust.aims.service;

public class PlaceOrderService {

    /**
     * Tính phí vận chuyển đơn hàng theo quy chuẩn hệ thống AIMS.
     * * @param orderAmount Tổng giá trị tiền hàng (VND)
     * @param weight      Tổng khối lượng hàng đặt (kg)
     * @param city        Tên tỉnh/thành phố nhận hàng
     * @return Phí vận chuyển áp dụng (VND)
     */
    public int calculateShippingFee(int orderAmount, double weight, String city) {
        // Quy định đặc tả: Đơn hàng có giá trị trên 100,000 VND được miễn phí vận chuyển hoàn toàn
        if (orderAmount > 100000) {
            return 0;
        }

        // Kiểm tra phân vùng địa lý: Nội thành (Hà Nội hoặc TP.HCM)
        if (city != null && (city.equalsIgnoreCase("Hanoi") || city.equalsIgnoreCase("HCM"))) {
            if (weight <= 3.0) {
                return 22000; // Phí cố định cho 3kg đầu tiên
            } else {
                // Khối lượng vượt quá 3kg sẽ bị tính phụ trội.
                // Mỗi kg phụ trội tiếp theo (làm tròn lên) cộng thêm 2,500 VND.
                double extraWeight = Math.ceil(weight - 3.0);
                return 22000 + (int) (extraWeight * 2500);
            }
        }

        // Phân vùng địa lý: Ngoại thành (Các tỉnh/thành phố khác) - Phí cố định 30,000 VND
        return 30000;
    }

    /**
     * Xác thực số lượng sản phẩm người dùng muốn đặt có đáp ứng được số lượng tồn kho hay không.
     * * @param quantityInStock   Số lượng mặt hàng hiện có thực tế trong kho hệ thống
     * @param requestedQuantity Số lượng mặt hàng khách yêu cầu đặt mua
     * @return true nếu kho đáp ứng đủ và số lượng đặt hợp lệ, ngược lại trả về false
     */
    public boolean validateMediaAvailability(int quantityInStock, int requestedQuantity) {
        // Số lượng đặt mua bắt buộc phải lớn hơn hoặc bằng 1
        if (requestedQuantity <= 0) {
            return false;
        }
        // Số lượng đặt mua không được phép vượt quá số lượng hiện có trong kho hàng
        return requestedQuantity <= quantityInStock;
    }
}