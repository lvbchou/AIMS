package com.hust.aims.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("Kiểm thử đơn vị: Tính phí vận chuyển đơn hàng (AIMS)")
class CalculateShippingFeeTest {

    private PlaceOrderService placeOrderController;

    @BeforeEach
    void setUp() {
        placeOrderController = new PlaceOrderService();
    }

    @ParameterizedTest(name = "[{index}] Đơn {0}đ | Nặng {1}kg | Tỉnh/TP: {2} -> Phí ship kỳ vọng: {3}đ")
    @CsvSource({
            // 1. Kiểm thử biên quanh mốc miễn phí vận chuyển (100,000 VND)
            "99999,  2.0,  Hanoi,     22000", // Sát dưới 100k, tính phí gốc nội thành
            "100000, 2.0,  Hanoi,     22000", // Đúng biên 100k, vẫn tính phí gốc nội thành
            "100001, 5.0,  Hai Phong, 0    ", // Sát trên 100k, được miễn phí vận chuyển hoàn toàn
            "250000, 10.0, HCM,       0    ", // Nằm sâu trong vùng miễn phí vận chuyển

            // 2. Kiểm thừ biên khối lượng & phụ trội cho Nội thành (Hà Nội / HCM)
            "50000,  3.0,  Hanoi,     22000", // Biên tối đa của phí gốc nội thành (đúng 3kg)
            "50000,  3.1,  Hanoi,     24500", // Vừa vượt 3kg (3.1kg), phụ trội tính 1kg tiếp theo = 22k + 2.5k
            "75000,  5.0,  HCM,       27000", // Khối lượng nội thành lớn (5kg) = 22k + (5-3)*2.5k
            "75000,  5.5,  HCM,       29500", // Khối lượng nội thành lẻ (5.5kg làm tròn lên thành 3kg phụ trội) = 22k + 3*2.5k

            // 3. Kiểm thử phân vùng Ngoại thành (Cố định phí 30,000 VND bất kể cân nặng nếu giá đơn <= 100k)
            "30000,  1.0,  Hai Phong, 30000", // Ngoại thành khối lượng nhỏ
            "30000,  15.0, Danang,    30000"  // Ngoại thành khối lượng cực lớn (không tính phụ trội cân nặng)
    })
    @DisplayName("Các kịch bản tổ hợp và phân tích giá trị biên cho Phí vận chuyển")
    void testCalculateShippingFee(int orderAmount, double weight, String city, int expectedFee) {
        // Act
        int actualFee = placeOrderController.calculateShippingFee(orderAmount, weight, city);

        // Assert
        assertEquals(expectedFee, actualFee,
                String.format("Sai logic tính phí ship cho đơn hàng %dđ, nặng %.1fkg tại %s", orderAmount, weight, city));
    }
}