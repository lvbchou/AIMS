package com.aims.service.notification;

import com.aims.dto.order.OrderConfirmationDTO;
import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.util.Locale;

@Component
public class OrderNotificationContentBuilder {

    public NotificationMessage buildOrderSuccessMessage(String orderId, OrderConfirmationDTO dto) {
        return new NotificationMessage(
                "AIMS order confirmation - " + orderId,
                buildPlainText(orderId, dto),
                buildHtml(orderId, dto));
    }

    private String buildPlainText(String orderId, OrderConfirmationDTO dto) {
        return "Payment successful\n"
                + "Order ID: " + orderId + "\n"
                + "Order: " + value(dto.getOrderName()) + "\n"
                + "Customer: " + value(dto.getCustomerName()) + "\n"
                + "Phone: " + value(dto.getPhoneNumber()) + "\n"
                + "Address: " + value(dto.getShippingAddress()) + ", " + value(dto.getProvince()) + "\n"
                + "Total amount: " + formatCurrency(dto.getTotalAmountToBePaid()) + "\n"
                + "Transaction ID: " + value(dto.getTransactionId()) + "\n"
                + "Transaction content: " + value(dto.getTransactionContent()) + "\n"
                + "Transaction time: " + value(dto.getTransactionDatetimeDisplay()) + "\n";
    }

    private String buildHtml(String orderId, OrderConfirmationDTO dto) {
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
