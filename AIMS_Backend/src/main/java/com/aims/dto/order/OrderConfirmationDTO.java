package com.aims.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Coupling level: Data Coupling.
 * Cohesion level: Functional Cohesion.
 * <p>
 * This DTO packages the confirmation values shown after a successful payment.
 *
 * @author Team 03
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderConfirmationDTO {

    private String customerName;
    private String phoneNumber;
    private String shippingAddress;
    private String province;
    private long totalAmountToBePaid;
    private String transactionId;
    private String transactionContent;
    /** Tên ngắn gọn của đơn hàng — hiển thị trên trang success. */
    private String orderName;
    /** Format DD/MM/YYYY HH:MM:SS per UC003 Table 3. */
    private String transactionDatetimeDisplay;
}
