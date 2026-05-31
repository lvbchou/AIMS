package com.aims.service;

import com.aims.constants.OrderStatusValues;
import com.aims.constants.PaymentTransactionStatusValues;
import com.aims.dto.*;
import com.aims.entity.*;
import com.aims.exception.*;
import com.aims.repository.*;
import com.aims.subsystem.IPaymentQRCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PayOrderService} — UC003 Pay Order.
 *
 * <p>Covers:
 * <ul>
 *     <li>Main Flow: invoice screen, QR code request, VietQR callback (SUCCESS), confirmation</li>
 *     <li>Alternative Flows: payment already completed, reuse existing PENDING transaction</li>
 *     <li>Exception Flows: order not found, invoice missing, delivery missing, order not payable,
 *         callback FAILED, invalid callback payload, transaction not found</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class PayOrderServiceTest {

    private static final String ORDER_ID = "ORD-001";
    private static final String INVOICE_ID = "INV-001";
    private static final String TXN_ID = "TX-001";

    @Mock private OrderRepository orderRepository;
    @Mock private InvoiceRepository invoiceRepository;
    @Mock private DeliveryRepository deliveryRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private PaymentTransactionRepository paymentTransactionRepository;
    @Mock private IPaymentQRCode paymentQRCode;

    @InjectMocks
    private PayOrderService payOrderService;

    // ── shared fixtures ──────────────────────────────────────────────────

    private Order awaitingOrder;
    private Invoice invoice;
    private Delivery delivery;
    private Product product;
    private OrderItem orderItem;

    @BeforeEach
    void setUp() {
        awaitingOrder = new Order();
        awaitingOrder.setOrderId(ORDER_ID);
        awaitingOrder.setStatus(OrderStatusValues.AWAITING_PAYMENT);
        awaitingOrder.setCreatedAt(Instant.now());

        invoice = new Invoice();
        invoice.setInvoiceId(INVOICE_ID);
        invoice.setOrderId(ORDER_ID);
        invoice.setIssueDate(LocalDate.of(2026, 5, 3));
        invoice.setSubTotalExVat(360_000);
        invoice.setSubTotalIncVat(400_000);
        invoice.setShippingFee(30_000);

        delivery = new Delivery();
        delivery.setOrderId(ORDER_ID);
        delivery.setRecipientName("Nguyen Van A");
        delivery.setPhoneNumber("0912345678");
        delivery.setEmail("a@aims.vn");
        delivery.setDeliveryProvince("Hanoi");
        delivery.setDetailAddress("1 Pham Van Bach, Cau Giay");

        product = new Product();
        product.setProductId(1);
        product.setTitle("DVD Mai");
        product.setSellingPrice(18_000);
        product.setStatus("active");

        OrderItemId itemId = new OrderItemId(ORDER_ID, 1);
        orderItem = new OrderItem();
        orderItem.setId(itemId);
        orderItem.setOrder(awaitingOrder);
        orderItem.setProduct(product);
        orderItem.setQuantity(2);
    }

    // ── helpers ──────────────────────────────────────────────────────────

    private void stubOrderFound() {
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(awaitingOrder));
    }

    private void stubInvoiceFound() {
        when(invoiceRepository.findByOrderOrderId(ORDER_ID)).thenReturn(Optional.of(invoice));
    }

    private void stubDeliveryFound() {
        when(deliveryRepository.findById(ORDER_ID)).thenReturn(Optional.of(delivery));
    }

    private PaymentTransaction buildPendingTxn() {
        PaymentTransaction txn = new PaymentTransaction();
        txn.setTransactionId(TXN_ID);
        txn.setInvoiceId(INVOICE_ID);
        txn.setContent("Order #" + ORDER_ID);
        txn.setPaymentMethod("VIETQR");
        txn.setTransactionTime(Instant.now());
        txn.setStatus(PaymentTransactionStatusValues.PENDING);
        return txn;
    }

    private PaymentTransaction buildSuccessTxn() {
        PaymentTransaction txn = buildPendingTxn();
        txn.setStatus(PaymentTransactionStatusValues.SUCCESS);
        return txn;
    }

    // ╔═══════════════════════════════════════════════════════════════════╗
    // ║  MAIN FLOW — getInvoiceScreen                                    ║
    // ╚═══════════════════════════════════════════════════════════════════╝

    @Nested
    @DisplayName("getInvoiceScreen — UC003 Main Flow Step 2 (Invoice projection)")
    class GetInvoiceScreenTests {

        @Test
        @DisplayName("returns invoice projection with correct totals and line items")
        void returnsInvoiceProjection() {
            stubOrderFound();
            stubInvoiceFound();
            stubDeliveryFound();
            when(orderItemRepository.findAllWithProductByOrderId(ORDER_ID))
                    .thenReturn(List.of(orderItem));

            InvoiceScreenDTO result = payOrderService.getInvoiceScreen(ORDER_ID);

            assertThat(result.getOrderId()).isEqualTo(ORDER_ID);
            assertThat(result.getInvoiceId()).isEqualTo(INVOICE_ID);
            assertThat(result.getTotalProductPriceExclVat()).isEqualTo(360_000);
            assertThat(result.getTotalProductPriceInclVat()).isEqualTo(400_000);
            assertThat(result.getDeliveryFee()).isEqualTo(30_000);
            assertThat(result.getTotalAmountToBePaid()).isEqualTo(430_000);

            assertThat(result.getLineItems()).hasSize(1);
            InvoiceLineItemDTO line = result.getLineItems().getFirst();
            assertThat(line.getProductTitle()).isEqualTo("DVD Mai");
            assertThat(line.getQuantity()).isEqualTo(2);
            assertThat(line.getUnitSellingPrice()).isEqualTo(18_000);
            assertThat(line.getLineTotalSellingPrice()).isEqualTo(36_000);
        }

        @Test
        @DisplayName("throws OrderNotFoundException when order does not exist")
        void throwsWhenOrderNotFound() {
            when(orderRepository.findById("NONE")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> payOrderService.getInvoiceScreen("NONE"))
                    .isInstanceOf(OrderNotFoundException.class);
        }

        @Test
        @DisplayName("throws OrderNotPayableException when order is not AWAITING_PAYMENT")
        void throwsWhenOrderNotPayable() {
            awaitingOrder.setStatus(OrderStatusValues.PENDING_PROCESSING);
            stubOrderFound();

            assertThatThrownBy(() -> payOrderService.getInvoiceScreen(ORDER_ID))
                    .isInstanceOf(OrderNotPayableException.class);
        }

        @Test
        @DisplayName("throws InvoiceNotFoundException when invoice is missing")
        void throwsWhenInvoiceMissing() {
            stubOrderFound();
            when(invoiceRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> payOrderService.getInvoiceScreen(ORDER_ID))
                    .isInstanceOf(InvoiceNotFoundException.class);
        }

        @Test
        @DisplayName("throws OrderNotPayableException when delivery profile is missing")
        void throwsWhenDeliveryMissing() {
            stubOrderFound();
            stubInvoiceFound();
            when(deliveryRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> payOrderService.getInvoiceScreen(ORDER_ID))
                    .isInstanceOf(OrderNotPayableException.class);
        }
    }

    // ╔═══════════════════════════════════════════════════════════════════╗
    // ║  MAIN FLOW — requestVietQrDisplay                                ║
    // ╚═══════════════════════════════════════════════════════════════════╝

    @Nested
    @DisplayName("requestVietQrDisplay — UC003 Steps 3–5 (QR code generation)")
    class RequestVietQrDisplayTests {

        @Test
        @DisplayName("creates new PENDING transaction and returns QR bundle")
        void createsNewTxnAndReturnsQr() {
            stubOrderFound();
            stubInvoiceFound();
            stubDeliveryFound();

            // no prior SUCCESS, no prior PENDING
            when(paymentTransactionRepository.existsByInvoiceIdAndStatus(INVOICE_ID,
                    PaymentTransactionStatusValues.SUCCESS)).thenReturn(false);
            when(paymentTransactionRepository.findFirstByInvoiceIdAndStatusOrderByTransactionTimeDesc(
                    INVOICE_ID, PaymentTransactionStatusValues.PENDING)).thenReturn(Optional.empty());

            PaymentTransaction savedTxn = buildPendingTxn();
            when(paymentTransactionRepository.save(any(PaymentTransaction.class))).thenReturn(savedTxn);

            QRCode qrCode = new QRCode();
            qrCode.setQrCode("QR_BASE64");
            qrCode.setQrLink("VIETQR-REF-INV-001");
            when(paymentQRCode.getQRCode(awaitingOrder)).thenReturn(qrCode);

            VietQRCodeResponseDTO result = payOrderService.requestVietQrDisplay(ORDER_ID);

            assertThat(result.getOrderId()).isEqualTo(ORDER_ID);
            assertThat(result.getInvoiceId()).isEqualTo(INVOICE_ID);
            assertThat(result.getTransactionId()).isEqualTo(TXN_ID);
            assertThat(result.getQrCodeImageBase64()).isEqualTo("QR_BASE64");
            assertThat(result.getVietQrReference()).isEqualTo("VIETQR-REF-INV-001");
            assertThat(result.getTotalAmountToBePaid()).isEqualTo(430_000);

            // verify a new transaction was persisted
            ArgumentCaptor<PaymentTransaction> captor = ArgumentCaptor.forClass(PaymentTransaction.class);
            verify(paymentTransactionRepository).save(captor.capture());
            PaymentTransaction captured = captor.getValue();
            assertThat(captured.getTransactionId()).startsWith("TXN-");
            assertThat(captured.getPaymentMethod()).isEqualTo("VIETQR");
            assertThat(captured.getStatus()).isEqualTo(PaymentTransactionStatusValues.PENDING);
        }

        @Test
        @DisplayName("reuses existing PENDING transaction instead of creating a new one")
        void reusesExistingPendingTxn() {
            stubOrderFound();
            stubInvoiceFound();
            stubDeliveryFound();

            when(paymentTransactionRepository.existsByInvoiceIdAndStatus(INVOICE_ID,
                    PaymentTransactionStatusValues.SUCCESS)).thenReturn(false);

            PaymentTransaction existingPending = buildPendingTxn();
            when(paymentTransactionRepository.findFirstByInvoiceIdAndStatusOrderByTransactionTimeDesc(
                    INVOICE_ID, PaymentTransactionStatusValues.PENDING))
                    .thenReturn(Optional.of(existingPending));

            QRCode qrCode = new QRCode();
            qrCode.setQrCode("QR_BASE64");
            qrCode.setQrLink("VIETQR-REF-INV-001");
            when(paymentQRCode.getQRCode(awaitingOrder)).thenReturn(qrCode);

            VietQRCodeResponseDTO result = payOrderService.requestVietQrDisplay(ORDER_ID);

            assertThat(result.getTransactionId()).isEqualTo(TXN_ID);
            // save should NOT be called — we reused the existing PENDING txn
            verify(paymentTransactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws PaymentAlreadyCompletedException when payment already succeeded")
        void throwsWhenAlreadyPaid() {
            stubOrderFound();
            stubInvoiceFound();
            stubDeliveryFound();

            when(paymentTransactionRepository.existsByInvoiceIdAndStatus(INVOICE_ID,
                    PaymentTransactionStatusValues.SUCCESS)).thenReturn(true);

            assertThatThrownBy(() -> payOrderService.requestVietQrDisplay(ORDER_ID))
                    .isInstanceOf(PaymentAlreadyCompletedException.class);
        }
    }

    // ╔═══════════════════════════════════════════════════════════════════╗
    // ║  MAIN FLOW — handleVietQrPaymentCallback                         ║
    // ╚═══════════════════════════════════════════════════════════════════╝

    @Nested
    @DisplayName("handleVietQrPaymentCallback — UC003 Step 7 (VietQR callback)")
    class HandleCallbackTests {

        @Test
        @DisplayName("SUCCESS callback → updates transaction to SUCCESS and order to PENDING_PROCESSING")
        void successCallback() {
            PaymentTransaction txn = buildPendingTxn();
            when(paymentTransactionRepository.findById(TXN_ID)).thenReturn(Optional.of(txn));
            when(invoiceRepository.findById(INVOICE_ID)).thenReturn(Optional.of(invoice));
            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(awaitingOrder));

            VietQrCallbackRequestDTO req = VietQrCallbackRequestDTO.builder()
                    .transactionId(TXN_ID)
                    .paymentStatus("SUCCESS")
                    .build();

            payOrderService.handleVietQrPaymentCallback(req);

            assertThat(txn.getStatus()).isEqualTo(PaymentTransactionStatusValues.SUCCESS);
            assertThat(txn.getTransactionTime()).isNotNull();
            verify(paymentTransactionRepository).save(txn);

            assertThat(awaitingOrder.getStatus()).isEqualTo(OrderStatusValues.PENDING_PROCESSING);
            verify(orderRepository).save(awaitingOrder);
        }

        @Test
        @DisplayName("FAILED callback → updates transaction to FAILED, order status unchanged")
        void failedCallback() {
            PaymentTransaction txn = buildPendingTxn();
            when(paymentTransactionRepository.findById(TXN_ID)).thenReturn(Optional.of(txn));

            VietQrCallbackRequestDTO req = VietQrCallbackRequestDTO.builder()
                    .transactionId(TXN_ID)
                    .paymentStatus("FAILED")
                    .build();

            payOrderService.handleVietQrPaymentCallback(req);

            assertThat(txn.getStatus()).isEqualTo(PaymentTransactionStatusValues.FAILED);
            verify(paymentTransactionRepository).save(txn);
            // order status must NOT change on failure
            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("Idempotent — skips processing when transaction already SUCCESS")
        void idempotentOnAlreadySuccess() {
            PaymentTransaction txn = buildSuccessTxn();
            when(paymentTransactionRepository.findById(TXN_ID)).thenReturn(Optional.of(txn));

            VietQrCallbackRequestDTO req = VietQrCallbackRequestDTO.builder()
                    .transactionId(TXN_ID)
                    .paymentStatus("SUCCESS")
                    .build();

            payOrderService.handleVietQrPaymentCallback(req);

            verify(paymentTransactionRepository, never()).save(any());
            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws IllegalArgumentException when transactionId is null")
        void throwsWhenTxnIdNull() {
            VietQrCallbackRequestDTO req = VietQrCallbackRequestDTO.builder()
                    .transactionId(null)
                    .paymentStatus("SUCCESS")
                    .build();

            assertThatThrownBy(() -> payOrderService.handleVietQrPaymentCallback(req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("transactionId");
        }

        @Test
        @DisplayName("throws IllegalArgumentException when transactionId is blank")
        void throwsWhenTxnIdBlank() {
            VietQrCallbackRequestDTO req = VietQrCallbackRequestDTO.builder()
                    .transactionId("   ")
                    .paymentStatus("SUCCESS")
                    .build();

            assertThatThrownBy(() -> payOrderService.handleVietQrPaymentCallback(req))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("throws PaymentTransactionNotFoundException for unknown transactionId")
        void throwsWhenTxnNotFound() {
            when(paymentTransactionRepository.findById("UNKNOWN")).thenReturn(Optional.empty());

            VietQrCallbackRequestDTO req = VietQrCallbackRequestDTO.builder()
                    .transactionId("UNKNOWN")
                    .paymentStatus("SUCCESS")
                    .build();

            assertThatThrownBy(() -> payOrderService.handleVietQrPaymentCallback(req))
                    .isInstanceOf(PaymentTransactionNotFoundException.class);
        }

        @Test
        @DisplayName("throws IllegalArgumentException when paymentStatus is invalid (not SUCCESS/FAILED)")
        void throwsWhenInvalidStatus() {
            VietQrCallbackRequestDTO req = VietQrCallbackRequestDTO.builder()
                    .transactionId(TXN_ID)
                    .paymentStatus("PROCESSING")
                    .build();

            assertThatThrownBy(() -> payOrderService.handleVietQrPaymentCallback(req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("SUCCESS or FAILED");
        }

        @Test
        @DisplayName("accepts VietQR payment code '00' as SUCCESS")
        void acceptsVietQrCode00() {
            PaymentTransaction txn = buildPendingTxn();
            when(paymentTransactionRepository.findById(TXN_ID)).thenReturn(Optional.of(txn));
            when(invoiceRepository.findById(INVOICE_ID)).thenReturn(Optional.of(invoice));
            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(awaitingOrder));

            VietQrCallbackRequestDTO req = VietQrCallbackRequestDTO.builder()
                    .transactionId(TXN_ID)
                    .paymentStatus("00")
                    .build();

            payOrderService.handleVietQrPaymentCallback(req);

            assertThat(txn.getStatus()).isEqualTo(PaymentTransactionStatusValues.SUCCESS);
        }

        @Test
        @DisplayName("accepts case-insensitive payment status 'success'")
        void acceptsCaseInsensitiveSuccess() {
            PaymentTransaction txn = buildPendingTxn();
            when(paymentTransactionRepository.findById(TXN_ID)).thenReturn(Optional.of(txn));
            when(invoiceRepository.findById(INVOICE_ID)).thenReturn(Optional.of(invoice));
            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(awaitingOrder));

            VietQrCallbackRequestDTO req = VietQrCallbackRequestDTO.builder()
                    .transactionId(TXN_ID)
                    .paymentStatus("success")
                    .build();

            payOrderService.handleVietQrPaymentCallback(req);

            assertThat(txn.getStatus()).isEqualTo(PaymentTransactionStatusValues.SUCCESS);
        }
    }

    // ╔═══════════════════════════════════════════════════════════════════╗
    // ║  MAIN FLOW — handleVietQrWebhook (raw VietQR JSON)               ║
    // ╚═══════════════════════════════════════════════════════════════════╝

    @Nested
    @DisplayName("handleVietQrWebhook — VietQR bank callback JSON")
    class HandleWebhookTests {

        @Test
        @DisplayName("maps VietQR webhook to pending AIMS transaction and marks order paid")
        void mapsWebhookToPendingTxn() {
            PaymentTransaction txn = buildPendingTxn();
            stubInvoiceFound();
            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(awaitingOrder));
            when(paymentTransactionRepository.findFirstByInvoiceIdAndStatusOrderByTransactionTimeDesc(
                    INVOICE_ID, PaymentTransactionStatusValues.PENDING))
                    .thenReturn(Optional.of(txn));
            when(paymentTransactionRepository.findById(TXN_ID)).thenReturn(Optional.of(txn));
            when(invoiceRepository.findById(INVOICE_ID)).thenReturn(Optional.of(invoice));

            String webhook = """
                    {"paymentStatus":"00","content":"Order #ORD-001","transactionId":"VIETQR-TX-999"}
                    """;

            PaymentResult paymentResult = new PaymentResult(
                    "00", "VietQR Callback Payload", "Order #ORD-001", "VIETQR-TX-999", 1);
            when(paymentQRCode.checkPaymentStatus(webhook)).thenReturn(paymentResult);

            payOrderService.handleVietQrWebhook(webhook);

            assertThat(txn.getStatus()).isEqualTo(PaymentTransactionStatusValues.SUCCESS);
            assertThat(awaitingOrder.getStatus()).isEqualTo(OrderStatusValues.PENDING_PROCESSING);
        }
    }

    // ╔═══════════════════════════════════════════════════════════════════╗
    // ║  MAIN FLOW — getOrderConfirmation                                ║
    // ╚═══════════════════════════════════════════════════════════════════╝

    @Nested
    @DisplayName("getOrderConfirmation — UC003 Steps 8–10 (Post-payment confirmation)")
    class GetOrderConfirmationTests {

        @Test
        @DisplayName("returns confirmation with delivery info and transaction details")
        void returnsConfirmation() {
            Order paidOrder = new Order();
            paidOrder.setOrderId(ORDER_ID);
            paidOrder.setStatus(OrderStatusValues.PENDING_PROCESSING);
            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(paidOrder));
            stubInvoiceFound();
            stubDeliveryFound();

            PaymentTransaction txn = buildSuccessTxn();
            txn.setTransactionTime(Instant.parse("2026-05-03T12:00:00Z"));
            when(paymentTransactionRepository.findFirstByInvoiceIdAndStatusOrderByTransactionTimeDesc(
                    INVOICE_ID, PaymentTransactionStatusValues.SUCCESS))
                    .thenReturn(Optional.of(txn));

            OrderConfirmationDTO result = payOrderService.getOrderConfirmation(ORDER_ID);

            assertThat(result.getCustomerName()).isEqualTo("Nguyen Van A");
            assertThat(result.getPhoneNumber()).isEqualTo("0912345678");
            assertThat(result.getShippingAddress()).isEqualTo("1 Pham Van Bach, Cau Giay");
            assertThat(result.getProvince()).isEqualTo("Hanoi");
            assertThat(result.getTotalAmountToBePaid()).isEqualTo(430_000);
            assertThat(result.getTransactionId()).isEqualTo(TXN_ID);
            assertThat(result.getTransactionContent()).isEqualTo("Order #" + ORDER_ID);
            // Verify datetime format is DD/MM/YYYY HH:MM:SS in Asia/Ho_Chi_Minh timezone
            assertThat(result.getTransactionDatetimeDisplay()).matches("\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}:\\d{2}");
        }

        @Test
        @DisplayName("throws OrderNotFoundException when order does not exist")
        void throwsWhenOrderNotFound() {
            when(orderRepository.findById("NONE")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> payOrderService.getOrderConfirmation("NONE"))
                    .isInstanceOf(OrderNotFoundException.class);
        }

        @Test
        @DisplayName("throws OrderNotPayableException when order is still AWAITING_PAYMENT")
        void throwsWhenOrderStillAwaiting() {
            stubOrderFound(); // awaitingOrder has AWAITING_PAYMENT status

            assertThatThrownBy(() -> payOrderService.getOrderConfirmation(ORDER_ID))
                    .isInstanceOf(OrderNotPayableException.class)
                    .hasMessageContaining("successful VietQR payment");
        }

        @Test
        @DisplayName("throws OrderNotPayableException when no successful transaction exists")
        void throwsWhenNoSuccessTxn() {
            Order paidOrder = new Order();
            paidOrder.setOrderId(ORDER_ID);
            paidOrder.setStatus(OrderStatusValues.PENDING_PROCESSING);
            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(paidOrder));
            stubInvoiceFound();
            stubDeliveryFound();

            when(paymentTransactionRepository.findFirstByInvoiceIdAndStatusOrderByTransactionTimeDesc(
                    INVOICE_ID, PaymentTransactionStatusValues.SUCCESS))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> payOrderService.getOrderConfirmation(ORDER_ID))
                    .isInstanceOf(OrderNotPayableException.class);
        }
    }
}
