// Coupling Level: Data Coupling
// Cohesion Level: Functional Cohesion
// Reason for Coupling: Its constructor accepts only basic primitive string attributes
//   (amount, currency, urls) without coupling to domain models like Invoice or Order.
// Reason for Cohesion: Every field is strictly aligned around representing the PayPal
//   Create Order API request structure.
/**
 * SOLID Principles Analysis (refactored — R4):
 * - **SRP Compliance**: The previous version violated SRP by owning a {@code toJsonString()}
 *   method that built a Jackson {@link com.fasterxml.jackson.databind.ObjectMapper} on every
 *   call. A DTO's sole responsibility is to carry data. Serialization logic now lives in
 *   {@link PayPalResponseMapper#serializeCreateOrder(CreateOrderRequest)}, which reuses the
 *   shared {@code MAPPER} singleton.
 */
package com.aims.subsystem.paypal;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * CreateOrderRequest — immutable data holder for the parameters sent to
 * the PayPal {@code POST /v2/checkout/orders} endpoint.
 *
 * <p>Serialization is performed by
 * {@link PayPalResponseMapper#serializeCreateOrder(CreateOrderRequest)}, which
 * reuses the shared {@link com.fasterxml.jackson.databind.ObjectMapper} instance.</p>
 *
 * <p>Note: The PayPal Create Order payload is a nested structure that cannot be
 * produced by direct field mapping alone. The nested JSON construction logic
 * therefore lives in {@link PayPalResponseMapper#serializeCreateOrder}, not here.</p>
 */
public class CreateOrderRequest {

    private final String amount;
    private final String currencyCode;
    private final String invoiceId;
    private final String returnUrl;
    private final String cancelUrl;

    public CreateOrderRequest(String amount, String currencyCode, String invoiceId,
                              String returnUrl, String cancelUrl) {
        this.amount = amount;
        this.currencyCode = currencyCode;
        this.invoiceId = invoiceId;
        this.returnUrl = returnUrl;
        this.cancelUrl = cancelUrl;
    }

    public String getAmount()       { return amount; }
    public String getCurrencyCode() { return currencyCode; }
    public String getInvoiceId()    { return invoiceId; }
    public String getReturnUrl()    { return returnUrl; }
    public String getCancelUrl()    { return cancelUrl; }
}
