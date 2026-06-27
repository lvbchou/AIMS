package com.aims.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload VietQR gửi về hệ thống qua webhook callback.
 * Mapping theo đúng format API thực tế của VietQR Sandbox.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VietQRCallbackRequestDTO {

    /** Số tài khoản ngân hàng nhận tiền */
    private String bankAccount;

    /**
     * Nội dung chuyển khoản — QUAN TRỌNG:
     * VietQRCallbackHandler.getOrderIdFromContent() dùng field này
     * để đối soát và extract orderId
     */
    private String content;

    /** Số tiền giao dịch */
    private long amount;

    /**
     * Loại giao dịch:
     * "C" = Credit (tiền vào) — luôn dùng giá trị này cho thanh toán
     * "D" = Debit  (tiền ra)
     */
    private String transType;

    /** Mã ngân hàng. Ví dụ: "MB", "VCB" */
    private String bankCode;

    /** Mã giao dịch phía VietQR */
    private String transactionId;

    /**
     * Trạng thái thanh toán do VietQR gửi về.
     * Ví dụ: "SUCCESS" hoặc "FAILED". Bắt buộc cho luồng xử lý thanh toán.
     */
    private String paymentStatus;

    /**
     * Checksum để verify callback hợp lệ.
     * VietQRCallbackHandler.verifyChecksum(apiKey) dùng field này.
     */
    private String checksum;
}
