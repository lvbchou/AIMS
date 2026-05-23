package com.aims.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link QRCode}.
 *
 * Đã rút gọn: CHỈ tập trung vào duy nhất 1 method phức tạp nhất: parseQRCodeResponse()
 */
class QRCodeTest {

    // ╔═══════════════════════════════════════════════════════════════════╗
    // ║  parseQRCodeResponse()                                             ║
    // ╚═══════════════════════════════════════════════════════════════════╝

    @Nested
    @DisplayName("parseQRCodeResponse() — parses key-value response string into QRCode fields")
    class ParseQRCodeResponseTests {

        @Test
        @DisplayName("parses semicolon-delimited response correctly")
        void parsesSemicolonDelimited() {
            QRCode qr = new QRCode();
            String response = "qrCode:ABC123;qrLink:https://vietqr.net/qr/ABC123;bankCode:970436;bankName:Vietcombank;bankAccount:1234567890;orderId:ORD-001;expireAt:2026-06-01T23:59:59";

            qr.parseQRCodeResponse(response);

            assertThat(qr.getQrCode()).isEqualTo("ABC123");
            assertThat(qr.getQrLink()).isEqualTo("https://vietqr.net/qr/ABC123");
            assertThat(qr.getBankCode()).isEqualTo("970436");
            assertThat(qr.getBankName()).isEqualTo("Vietcombank");
            assertThat(qr.getBankAccount()).isEqualTo("1234567890");
            assertThat(qr.getOrderId()).isEqualTo("ORD-001");
            assertThat(qr.getExpireAt()).isEqualTo(LocalDateTime.of(2026, 6, 1, 23, 59, 59));
        }

        @Test
        @DisplayName("parses ampersand-delimited response correctly")
        void parsesAmpersandDelimited() {
            QRCode qr = new QRCode();
            String response = "qrCode=XYZ789&bankCode=970436&orderId=ORD-002";

            qr.parseQRCodeResponse(response);

            assertThat(qr.getQrCode()).isEqualTo("XYZ789");
            assertThat(qr.getBankCode()).isEqualTo("970436");
            assertThat(qr.getOrderId()).isEqualTo("ORD-002");
        }

        @Test
        @DisplayName("parses comma-delimited response correctly")
        void parsesCommaDelimited() {
            QRCode qr = new QRCode();
            String response = "qrCode:QR111,bankName:ACB,orderId:ORD-003";

            qr.parseQRCodeResponse(response);

            assertThat(qr.getQrCode()).isEqualTo("QR111");
            assertThat(qr.getBankName()).isEqualTo("ACB");
            assertThat(qr.getOrderId()).isEqualTo("ORD-003");
        }

        @Test
        @DisplayName("parses pipe-delimited response correctly")
        void parsesPipeDelimited() {
            QRCode qr = new QRCode();
            String response = "qrCode:QR222|bankCode:970415|orderId:ORD-004";

            qr.parseQRCodeResponse(response);

            assertThat(qr.getQrCode()).isEqualTo("QR222");
            assertThat(qr.getBankCode()).isEqualTo("970415");
            assertThat(qr.getOrderId()).isEqualTo("ORD-004");
        }

        @Test
        @DisplayName("does nothing when response is null")
        void handlesNullResponse() {
            QRCode qr = new QRCode();
            qr.parseQRCodeResponse(null);

            assertThat(qr.getQrCode()).isNull();
            assertThat(qr.getOrderId()).isNull();
        }

        @Test
        @DisplayName("does nothing when response is empty")
        void handlesEmptyResponse() {
            QRCode qr = new QRCode();
            qr.parseQRCodeResponse("");

            assertThat(qr.getQrCode()).isNull();
        }

        @Test
        @DisplayName("does nothing when response is blank (whitespace only)")
        void handlesBlankResponse() {
            QRCode qr = new QRCode();
            qr.parseQRCodeResponse("   ");

            assertThat(qr.getQrCode()).isNull();
        }

        @Test
        @DisplayName("ignores tokens without key-value separator")
        void ignoresInvalidTokens() {
            QRCode qr = new QRCode();
            String response = "qrCode:ABC;invalidtoken;bankCode:970436";

            qr.parseQRCodeResponse(response);

            assertThat(qr.getQrCode()).isEqualTo("ABC");
            assertThat(qr.getBankCode()).isEqualTo("970436");
        }

        @Test
        @DisplayName("ignores unknown keys")
        void ignoresUnknownKeys() {
            QRCode qr = new QRCode();
            String response = "qrCode:ABC;unknownKey:value123;bankCode:970436";

            qr.parseQRCodeResponse(response);

            assertThat(qr.getQrCode()).isEqualTo("ABC");
            assertThat(qr.getBankCode()).isEqualTo("970436");
        }
    }
}
