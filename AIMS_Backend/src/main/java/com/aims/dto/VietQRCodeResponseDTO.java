package com.aims.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Coupling level: Data Coupling.
 * Cohesion level: Functional Cohesion.
 * <p>
 * This DTO carries only the QR response fields that the client needs to render
 * the payment screen.
 *
 * @author Team 03
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VietQRCodeResponseDTO {

    private String orderId;
    private String invoiceId;
    private String transactionId;
    private String vietQrReference;
    private String qrCodeImageBase64;
    private long totalAmountToBePaid;
    /** Nội dung chuyển tiền — phải truyền chính xác vào Test Callback */
    private String content;
}