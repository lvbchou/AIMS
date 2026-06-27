package com.aims.subsystem.vietqr;

import com.aims.subsystem.vietqr.callback.VietQRCallbackHandler;
import com.aims.entity.PaymentResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link VietQRCallbackHandler} (sd CheckPaymentStatus).
 */
class VietQRCallbackHandlerTest {

    private static final String API_KEY = "test-api-key";

    // ╔═══════════════════════════════════════════════════════════════════╗
    // ║  checkSuccess()                                                    ║
    // ╚═══════════════════════════════════════════════════════════════════╝

    @Nested
    @DisplayName("checkSuccess() — determines if callback indicates a successful payment")
    class CheckSuccessTests {

        @Test
        @DisplayName("returns true when paymentStatus is 'SUCCESS'")
        void returnsTrueForSuccess() {
            String json = """
                    {"paymentStatus": "SUCCESS", "transactionId": "TX-001"}
                    """;
            VietQRCallbackHandler handler = new VietQRCallbackHandler(json);

            assertThat(handler.checkSuccess()).isTrue();
        }

        @Test
        @DisplayName("returns true when paymentStatus is 'success' (case-insensitive)")
        void returnsTrueForLowercaseSuccess() {
            String json = """
                    {"paymentStatus": "success", "transactionId": "TX-002"}
                    """;
            VietQRCallbackHandler handler = new VietQRCallbackHandler(json);

            assertThat(handler.checkSuccess()).isTrue();
        }

        @Test
        @DisplayName("returns true when paymentStatus is '00' (VietQR error code for success)")
        void returnsTrueForCode00() {
            String json = """
                    {"paymentStatus": "00", "transactionId": "TX-003"}
                    """;
            VietQRCallbackHandler handler = new VietQRCallbackHandler(json);

            assertThat(handler.checkSuccess()).isTrue();
        }

        @Test
        @DisplayName("returns false when paymentStatus is 'FAILED'")
        void returnsFalseForFailed() {
            String json = """
                    {"paymentStatus": "FAILED", "transactionId": "TX-004"}
                    """;
            VietQRCallbackHandler handler = new VietQRCallbackHandler(json);

            assertThat(handler.checkSuccess()).isFalse();
        }

        @Test
        @DisplayName("returns false when paymentStatus is missing")
        void returnsFalseWhenStatusMissing() {
            String json = """
                    {"transactionId": "TX-005"}
                    """;
            VietQRCallbackHandler handler = new VietQRCallbackHandler(json);

            assertThat(handler.checkSuccess()).isFalse();
        }

        @Test
        @DisplayName("returns false when paymentStatus is empty string")
        void returnsFalseWhenStatusEmpty() {
            String json = """
                    {"paymentStatus": "", "transactionId": "TX-006"}
                    """;
            VietQRCallbackHandler handler = new VietQRCallbackHandler(json);

            assertThat(handler.checkSuccess()).isFalse();
        }

        @Test
        @DisplayName("returns false when callback data is invalid JSON")
        void returnsFalseForInvalidJson() {
            VietQRCallbackHandler handler = new VietQRCallbackHandler("not-valid-json");

            assertThat(handler.checkSuccess()).isFalse();
        }

        @Test
        @DisplayName("returns false when callback data is null")
        void returnsFalseForNullData() {
            VietQRCallbackHandler handler = new VietQRCallbackHandler(null);

            assertThat(handler.checkSuccess()).isFalse();
        }

        @Test
        @DisplayName("returns false when callback data is empty string")
        void returnsFalseForEmptyData() {
            VietQRCallbackHandler handler = new VietQRCallbackHandler("");

            assertThat(handler.checkSuccess()).isFalse();
        }
    }

    @Nested
    @DisplayName("verifyChecksum() — callback authenticity")
    class VerifyChecksumTests {

        @Test
        @DisplayName("returns true when checksum is omitted (sandbox)")
        void allowsMissingChecksum() {
            String json = """
                    {"paymentStatus":"SUCCESS","content":"Order #ORD-001"}
                    """;
            assertThat(new VietQRCallbackHandler(json).verifyChecksum(API_KEY)).isTrue();
        }

        @Test
        @DisplayName("returns false when checksum is present but api key is blank")
        void rejectsWhenApiKeyMissing() {
            String json = """
                    {"paymentStatus":"SUCCESS","checksum":"abc123"}
                    """;
            assertThat(new VietQRCallbackHandler(json).verifyChecksum("")).isFalse();
        }
    }

    @Nested
    @DisplayName("toPaymentResult() — maps callback to PaymentResult")
    class ToPaymentResultTests {

        @Test
        @DisplayName("maps successful payment with order content")
        void mapsSuccess() {
            String json = """
                    {"paymentStatus":"00","content":"Order #ORD-001","transactionId":"VIETQR-TX-1"}
                    """;
            PaymentResult result = new VietQRCallbackHandler(json).toPaymentResult();

            assertThat(result.getOrderId()).isEqualTo("Order #ORD-001");
            assertThat(result.getPaymentId()).isEqualTo("VIETQR-TX-1");
            assertThat(result.checkSuccess()).isTrue();
        }
    }
}
