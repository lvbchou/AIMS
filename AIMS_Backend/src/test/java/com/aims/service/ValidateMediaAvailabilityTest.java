package com.hust.aims.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("Kiểm thử đơn vị: Xác thực tính khả dụng của số lượng sản phẩm trong kho")
class ValidateMediaAvailabilityTest {

    private PlaceOrderService placeOrderController;

    @BeforeEach
    void setUp() {
        placeOrderController = new PlaceOrderService();
    }

    @ParameterizedTest(name = "[{index}] Tồn kho: {0} | Số lượng yêu cầu: {1} -> Kỳ vọng hợp lệ: {2}")
    @CsvSource({
            // 1. Phân vùng hợp lệ và các điểm biên hợp lệ
            "10, 1,  true ", // Biên dưới hợp lệ (Đặt tối thiểu 1 sản phẩm)
            "10, 5,  true ", // Khoảng giữa của phân vùng hợp lệ
            "10, 10, true ", // Biên trên hợp lệ (Đặt mua vừa khít toàn bộ số lượng trong kho)

            // 2. Các điểm biên không hợp lệ và phân vùng lỗi
            "10, 11, false", // Biên lỗi trên (Yêu cầu vượt quá hàng tồn kho đúng 1 đơn vị)
            "10, 20, false", // Nằm sâu trong phân vùng lỗi vượt quá tồn kho
            "10, 0,  false", // Biên lỗi dưới (Yêu cầu đặt mua bằng 0)
            "10, -5, false"  // Nằm sâu trong phân vùng lỗi số lượng âm
    })
    @DisplayName("Phân vùng tương đương và giá trị biên cho số lượng sản phẩm đặt hàng")
    void testValidateMediaAvailability(int quantityInStock, int requestedQuantity, boolean expectedResult) {
        // Act
        boolean actualResult = placeOrderController.validateMediaAvailability(quantityInStock, requestedQuantity);

        // Assert
        assertEquals(expectedResult, actualResult,
                String.format("Lỗi xác thực tính khả dụng khi Tồn kho = %d và Yêu cầu = %d", quantityInStock, requestedQuantity));
    }
}