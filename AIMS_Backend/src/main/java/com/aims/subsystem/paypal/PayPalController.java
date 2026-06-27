// Coupling Level: Stamp Coupling
// Cohesion Level: Sequential Cohesion
// Reason for Coupling: It implements IPaymentGateway and receives entire Invoice and Order objects as parameters, using only a small subset of their attributes (e.g. ID, total amount). It is also stamp-coupled to custom DTO structures like GatewayTransactionContext and GatewayTransactionResult.
// Reason for Cohesion: The methods coordinate a sequence of processing steps where the output of one operation (e.g., getting an access token or converting currency) is directly used as the input to the next step (creating the payload and sending the HTTP request via the boundary).
/**
 * SOLID Principles Analysis:
 * - **SRP (Single Responsibility Principle) Violation**: Handles both the gateway transaction flow (coordinating payments) and PayPal credentials token caching/lifecycle management. Token caching should belong to a dedicated authentication helper.
 * - **DIP (Dependency Inversion Principle) Violation**:
 *   1. Directly instantiates `PayPalBoundary` in the constructor instead of injecting it.
 *   2. Directly instantiates concrete `CurrencyConverter` and `FixedExchangeRateProvider` inside `createPayment()` instead of injecting them as abstractions.
 * 
 * **Improvement Direction**:
 * 1. Extract OAuth token lifecycle management into a dedicated class (`PayPalAuthService`).
 * 2. Inject `PayPalBoundary`, `CurrencyConverter`, and `ExchangeRateProvider` through the constructor to allow runtime substitution and easier mocking.
 */
package com.aims.subsystem.paypal;

import com.aims.dto.payment.GatewayTransactionContext;
import com.aims.dto.payment.GatewayTransactionResult;
import com.aims.entity.Invoice;
import com.aims.entity.Order;
import java.math.BigDecimal;
import com.aims.exception.PaymentException;
import com.aims.IPaymentGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PayPalController implements IPaymentGateway {

    private final String clientId;
    private final String clientSecret;
    private final String baseUrl;
    private final String returnUrl;
    private final String cancelUrl;

    private String accessToken;
    private long tokenExpiryTime;
    private final PayPalBoundary payPalBoundary;

    public PayPalController(
            @Value("${paypal.client.id}") String clientId,
            @Value("${paypal.client.secret}") String clientSecret,
            @Value("${paypal.base.url}") String baseUrl,
            @Value("${paypal.url.return}") String returnUrl,
            @Value("${paypal.url.cancel}") String cancelUrl) {

        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.baseUrl = baseUrl;
        this.returnUrl = returnUrl;
        this.cancelUrl = cancelUrl;
        this.payPalBoundary = new PayPalBoundary(baseUrl);
    }

    private String getAccessToken() throws PaymentException {
        long currentTime = System.currentTimeMillis();

        if (this.accessToken != null && currentTime < (this.tokenExpiryTime - 10000)) {
            return this.accessToken;
        }

        try {
            // if not, get new token:
            // generate auth header
            AccessTokenRequest request = new AccessTokenRequest(clientId, clientSecret);
            String authHeader = request.toAuthorizationHeader();

            // get token response string
            String responseString = payPalBoundary.getAccessToken(authHeader);

            // parse that string
            AccessTokenResponse response = new AccessTokenResponse();
            response.parseResponse(responseString);

            // update and return new access token
            this.accessToken = response.getAccessToken();
            this.tokenExpiryTime = System.currentTimeMillis() + (response.getExpiresIn() * 1000);

            return this.accessToken;
        } catch (Exception e) {
            throw new PaymentException("Failed to obtain PayPal access token: " + e.getMessage());
        }
    }

    @Override
    public GatewayTransactionContext createPayment(Invoice invoice) throws PaymentException {
        // Developer Mock Mode
        if (clientId != null && clientId.startsWith("AWmock")) {
            String mockToken = "MOCK-EC-" + System.currentTimeMillis();
            // Direct the frontend straight to the result page with the token
            String mockApprovalUrl = this.returnUrl + "?token=" + mockToken;
            return new GatewayTransactionContext(mockToken, mockApprovalUrl);
        }

        try {
            // get access token
            String token = getAccessToken();

            // get order total amount by invoice.getTotalAmount
            BigDecimal vndAmount = invoice.getTotalAmount();

            // convert that VND amount to USD by using CurrencyConverter
            CurrencyConverter converter = new CurrencyConverter(new FixedExchangeRateProvider());
            BigDecimal usdAmount = converter.convert(vndAmount, "VND", "USD");

            // create CreateOrderRequest with the neccessary fields
            CreateOrderRequest request = new CreateOrderRequest(
                    usdAmount.toString(),
                    "USD",
                    invoice.getId(),
                    this.returnUrl,
                    this.cancelUrl);

            // call boundary.createOrder(token,request)
            String responseString = payPalBoundary.createOrder(token, request);

            // get the response as CreateOrderResponse and call .parseResponse
            CreateOrderResponse response = new CreateOrderResponse();
            response.parseResponse(responseString);

            // return GatewayTransactionContext
            return new GatewayTransactionContext(response.getPaypalOrderId(), response.getApproveUrl());
        } catch (Exception e) {
            throw new PaymentException("Failed to create payment: " + e.getMessage());
        }
    }

    @Override
    public GatewayTransactionResult completePayment(Order order, String paypalOrderId) throws PaymentException {
        // Developer Mock Mode
        if (paypalOrderId != null && paypalOrderId.startsWith("MOCK-EC-")) {
            String transactionId = "MOCK-TX-" + System.currentTimeMillis();
            String orderId = order != null ? order.getOrderId() : "MOCK-ORDER-ID";
            String status = "COMPLETED";
            boolean success = true;
            String message = "Mock capture succeeded";
            return new GatewayTransactionResult(transactionId, orderId, status, success, message);
        }

        try {
            // get access token
            String token = getAccessToken();

            // call boundary to capture order
            String responseString = payPalBoundary.captureOrder(token, paypalOrderId);

            // parse the response string
            CaptureOrderResponse response = new CaptureOrderResponse();
            response.parseResponse(responseString);

            // return GatewayTransactionResult
            String orderId = order.getOrderId();
            boolean success = response.checkSuccess();
            String transactionId = response.getTransactionId();
            String status = success ? response.getStatus() : response.getErrorName();
            String message = success ? null : response.getErrorMessage();

            return new GatewayTransactionResult(transactionId, orderId, status, success, message);
        } catch (Exception e) {
            throw new PaymentException("Failed to complete payment: " + e.getMessage());
        }
    }

}
