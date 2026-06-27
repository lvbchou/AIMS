/**
 * SOLID Principles Analysis (refactored):
 * - **OCP/DIP Compliance**: The PayPal-specific field `paypalOrderId` has been renamed to
 *   `gatewayOrderId`. This DTO is now vendor-neutral. Any new gateway (VNPay, Stripe, MoMo)
 *   can use this same DTO without seeing a PayPal-named field that is confusing in their context.
 *
 * **Backward Compatibility**: A deprecated `getPaypalOrderId()` alias is provided so that
 * existing call sites continue to compile while they migrate to `getGatewayOrderId()`.
 */
package com.aims.dto.payment;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * GatewayTransactionContext — vendor-neutral DTO holding the context of a
 * payment session initiated with any external gateway.
 *
 * <p>Fields are deliberately generic: {@code gatewayOrderId} holds the
 * gateway-issued identifier (PayPal Order ID, VNPay txRef, etc.),
 * and {@code approveUrl} holds the URL the customer must visit to
 * authorise the payment.</p>
 */
@Getter
@Setter
@NoArgsConstructor
public class GatewayTransactionContext {

    /** The gateway-issued order or session identifier. */
    private String gatewayOrderId;

    /** The URL to which the customer should be redirected to authorise the payment. */
    private String approveUrl;

    /**
     * Constructs a context with both the gateway order ID and the approval URL.
     *
     * @param gatewayOrderId the gateway-issued identifier.
     * @param approveUrl     the authorisation redirect URL.
     */
    public GatewayTransactionContext(String gatewayOrderId, String approveUrl) {
        this.gatewayOrderId = gatewayOrderId;
        this.approveUrl = approveUrl;
    }

    /**
     * Alias method to satisfy {@code getApprovalUrl()} calls in the business logic.
     *
     * @return the approval URL (same as {@link #getApproveUrl()}).
     */
    public String getApprovalUrl() {
        return approveUrl;
    }

    /**
     * Backward-compatibility alias for call sites that still use the old
     * PayPal-specific field name.
     *
     * @return the gateway order ID.
     * @deprecated Use {@link #getGatewayOrderId()} instead.
     */
    @Deprecated
    public String getPaypalOrderId() {
        return gatewayOrderId;
    }

    /**
     * Backward-compatibility setter alias.
     *
     * @param paypalOrderId the gateway order ID.
     * @deprecated Use {@link #setGatewayOrderId(String)} instead.
     */
    @Deprecated
    public void setPaypalOrderId(String paypalOrderId) {
        this.gatewayOrderId = paypalOrderId;
    }
}
