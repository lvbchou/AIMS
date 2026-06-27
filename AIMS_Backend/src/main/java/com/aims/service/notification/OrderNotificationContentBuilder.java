package com.aims.service.notification;

import com.aims.dto.order.OrderConfirmationDTO;
import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.util.Locale;

@Component
public class OrderNotificationContentBuilder {

    public NotificationMessage buildOrderSuccessMessage(String orderId, OrderConfirmationDTO dto) {
        String cancelUrl = "http://localhost:4200/orders/" + orderId + "/cancel";
        return new NotificationMessage(
                "AIMS order confirmation - " + orderId,
                buildPlainText(orderId, dto, cancelUrl),
                buildHtml(orderId, dto, cancelUrl));
    }

    private String buildPlainText(String orderId, OrderConfirmationDTO dto, String cancelUrl) {
        return "Payment successful\n"
                + "Order ID: " + orderId + "\n"
                + "Order: " + value(dto.getOrderName()) + "\n"
                + "Customer: " + value(dto.getCustomerName()) + "\n"
                + "Phone: " + value(dto.getPhoneNumber()) + "\n"
                + "Address: " + value(dto.getShippingAddress()) + ", " + value(dto.getProvince()) + "\n"
                + "Total amount: " + formatCurrency(dto.getTotalAmountToBePaid()) + "\n"
                + "Transaction ID: " + value(dto.getTransactionId()) + "\n"
                + "Transaction content: " + value(dto.getTransactionContent()) + "\n"
                + "Transaction time: " + value(dto.getTransactionDatetimeDisplay()) + "\n"
                + "\n"
                + "Need to cancel your order? Visit: " + cancelUrl + "\n";
    }

    private String buildHtml(String orderId, OrderConfirmationDTO dto, String cancelUrl) {
        String rows = row("Order ID", orderId)
                + row("Order", dto.getOrderName())
                + row("Customer", dto.getCustomerName())
                + row("Phone", dto.getPhoneNumber())
                + row("Shipping address", value(dto.getShippingAddress()) + ", " + value(dto.getProvince()))
                + row("Total amount", formatCurrency(dto.getTotalAmountToBePaid()))
                + row("Transaction ID", dto.getTransactionId())
                + row("Transaction content", dto.getTransactionContent())
                + row("Transaction time", dto.getTransactionDatetimeDisplay());

        return """
                <div style="font-family:Arial,sans-serif;color:#222;line-height:1.5">
                  <h2 style="color:#005F7B">Payment Successful</h2>
                  <p>Thank you for your purchase. Your order has been confirmed.</p>
                  <table style="border-collapse:collapse;width:100%;max-width:640px">
                    __ROWS__
                  </table>
                  <div style="margin-top: 25px; padding: 15px; background-color: #fcf8e3; border: 1px solid #faebcc; border-radius: 4px; max-width: 610px;">
                    <p style="margin: 0 0 10px 0; color: #8a6d3b; font-weight: bold;">Need to cancel this order?</p>
                    <p style="margin: 0 0 15px 0; font-size: 14px; color: #666;">You can cancel this order and receive a full automatic refund before it is processed by our manager.</p>
                    <a href="__CANCEL_URL__" style="display:inline-block;padding:8px 16px;background-color:#d9534f;color:white;text-decoration:none;border-radius:4px;font-weight:bold;font-size:14px;">Cancel Order</a>
                  </div>
                </div>
                """.replace("__ROWS__", rows).replace("__CANCEL_URL__", cancelUrl);
    }

    public NotificationMessage buildOrderCancellationMessage(String orderId, OrderConfirmationDTO dto) {
        return new NotificationMessage(
                "AIMS order cancellation - " + orderId,
                buildCancellationPlainText(orderId, dto),
                buildCancellationHtml(orderId, dto));
    }

    private String buildCancellationPlainText(String orderId, OrderConfirmationDTO dto) {
        return "Order Cancelled and Fully Refunded\n"
                + "Order ID: " + orderId + "\n"
                + "Order: " + value(dto.getOrderName()) + "\n"
                + "Customer: " + value(dto.getCustomerName()) + "\n"
                + "Total refunded amount: " + formatCurrency(dto.getTotalAmountToBePaid()) + "\n"
                + "Original Transaction ID: " + value(dto.getTransactionId()) + "\n"
                + "We have processed a full refund to your original payment method.\n";
    }

    private String buildCancellationHtml(String orderId, OrderConfirmationDTO dto) {
        String rows = row("Order ID", orderId)
                + row("Order", dto.getOrderName())
                + row("Customer", dto.getCustomerName())
                + row("Total refunded amount", formatCurrency(dto.getTotalAmountToBePaid()))
                + row("Original Transaction ID", dto.getTransactionId());

        return """
                <div style="font-family:Arial,sans-serif;color:#222;line-height:1.5">
                  <h2 style="color:#d9534f">Order Cancelled & Refunded</h2>
                  <p>Your order has been successfully cancelled. A full refund has been issued to your original payment method.</p>
                  <table style="border-collapse:collapse;width:100%;max-width:640px">
                    __ROWS__
                  </table>
                  <p style="margin-top:20px;color:#666;font-size:12px;">If you have any questions, please contact our support.</p>
                </div>
                """.replace("__ROWS__", rows);
    }

    private String row(String label, String value) {
        return """
                <tr>
                  <td style="padding:8px 12px;border-bottom:1px solid #eee;color:#666;font-weight:700">%s</td>
                  <td style="padding:8px 12px;border-bottom:1px solid #eee">%s</td>
                </tr>
                """.formatted(escape(label), escape(value(value)));
    }

    private String formatCurrency(long amount) {
        NumberFormat formatter = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));
        return formatter.format(amount) + " VND";
    }

    private String value(String value) {
        return value == null || value.isBlank() ? "N/A" : value;
    }

    private String escape(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
