package com.aims.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.aims.IPaymentGateway;
import com.aims.dtos.GatewayTransactionContext;
import com.aims.dtos.GatewayTransactionResult;
import com.aims.entities.Invoice;
import com.aims.entities.Order;
import com.aims.exceptions.PaymentException;
import com.aims.repository.IOrderRepository;

class PayThroughPaymentGatewayServiceTest {

    @Mock
    private IPaymentGateway mockGateway;

    @Mock
    private IOrderRepository mockDatabase;

    @InjectMocks
    private PayThroughPaymentGatewayService paymentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreatePayment_Success() throws PaymentException {
        // Arrange
        Order mockOrder = new Order("ORDER_123");
        Invoice mockInvoice = new Invoice();
        mockInvoice.setId("ORDER_123");
        mockInvoice.setOrder(mockOrder);
        String expectedUrl = "https://sandbox.paypal.com/checkoutnow?token=ABC";

        GatewayTransactionContext mockContext = new GatewayTransactionContext("GW_ORDER_123", expectedUrl);
        // Use any() for Invoice because the service might create a new Invoice object
        // internally
        when(mockGateway.createPayment(any(Invoice.class))).thenReturn(mockContext);

        // Act
        String actualUrl = paymentService.createPayment(mockInvoice);

        // Assert
        assertEquals(expectedUrl, actualUrl);

        assertEquals("PENDING_PAYMENT", mockOrder.getStatus());
        verify(mockDatabase, times(1)).updateOrder(mockOrder);
    }

    @Test
    void testCreatePayment_NoOrder() throws PaymentException {
        // Arrange
        Invoice mockInvoice = new Invoice();
        mockInvoice.setId("ORDER_123");
        // Do not set an Order, so mockInvoice.getOrder() will return null

        String expectedUrl = "https://sandbox.paypal.com/checkoutnow?token=ABC";
        GatewayTransactionContext mockContext = new GatewayTransactionContext("GW_ORDER_123", expectedUrl);
        when(mockGateway.createPayment(any(Invoice.class))).thenReturn(mockContext);

        // Act
        String actualUrl = paymentService.createPayment(mockInvoice);

        // Assert
        assertEquals(expectedUrl, actualUrl);

        verify(mockDatabase, never()).updateOrder(any(Order.class));
    }

    @Test
    void testCreatePayment_InvoiceIsNull() {
        // Arrange
        Invoice nullInvoice = null;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            paymentService.createPayment(nullInvoice);
        });

        assertEquals("Invoice cannot be null", exception.getMessage());

        verify(mockDatabase, never()).updateOrder(any(Order.class));
    }

    @Test
    void testCompletePayment_Success() throws PaymentException {
        // Arrange
        String token = "TOKEN_ABC";
        Order mockOrder = new Order("ORDER_123");

        when(mockDatabase.findByToken(token)).thenReturn(mockOrder);

        GatewayTransactionResult mockResult = new GatewayTransactionResult("TX_123", "ORDER_123", "COMPLETED", true,
                "COMPLETED");
        when(mockGateway.completePayment(mockOrder, token)).thenReturn(mockResult);

        // Act
        paymentService.completePayment(token);

        // Assert
        assertEquals("PAID", mockOrder.getStatus());
        verify(mockDatabase, times(1)).updateOrder(mockOrder);
    }

    @Test
    void testCompletePayment_FailDueToPayPalRejection() throws PaymentException {
        // Arrange
        String token = "TOKEN_FAIL";
        Order mockOrder = new Order("ORDER_123");
        mockOrder.setStatus("PENDING_PAYMENT");

        when(mockDatabase.findByToken(token)).thenReturn(mockOrder);

        GatewayTransactionResult mockResult = new GatewayTransactionResult("TX_FAIL", "ORDER_123", "DENIED", false,
                "ORDER_NOT_APPROVED");
        when(mockGateway.completePayment(mockOrder, token)).thenReturn(mockResult);

        // Act & Assert
        PaymentException exception = assertThrows(PaymentException.class, () -> {
            paymentService.completePayment(token);
        });

        assertTrue(exception.getMessage().contains("ORDER_NOT_APPROVED"));

        assertEquals("PENDING_PAYMENT", mockOrder.getStatus());
        verify(mockDatabase, never()).updateOrder(mockOrder);
    }

    @Test
    void testCompletePayment_OrderNotFound() throws PaymentException {
        // Arrange
        String token = "TOKEN_INVALID";

        when(mockDatabase.findByToken(token)).thenReturn(null);

        // Act & Assert
        PaymentException exception = assertThrows(PaymentException.class, () -> {
            paymentService.completePayment(token);
        });

        assertEquals("Order not found for token.", exception.getMessage());

        verify(mockGateway, never()).completePayment(any(Order.class), anyString());
        verify(mockDatabase, never()).updateOrder(any(Order.class));
    }

}
