package com.aims.service.placeorder;

import com.aims.dto.order.DeliveryInfoRequest;
import com.aims.exception.InvalidOrderException;
import org.springframework.stereotype.Service;

@Service
public class DeliveryValidationService {
    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

    public ValidatedDeliveryInfo validate(DeliveryInfoRequest deliveryInfoRequest) {
        if (deliveryInfoRequest == null) {
            throw new InvalidOrderException("Delivery information is required.");
        }
        if (deliveryInfoRequest.getRecipientName() == null || deliveryInfoRequest.getRecipientName().trim().isEmpty()
                || deliveryInfoRequest.getPhoneNumber() == null || deliveryInfoRequest.getPhoneNumber().trim().isEmpty()
                || deliveryInfoRequest.getDeliveryProvince() == null
                || deliveryInfoRequest.getDeliveryProvince().trim().isEmpty()
                || deliveryInfoRequest.getDetailAddress() == null
                || deliveryInfoRequest.getDetailAddress().trim().isEmpty()) {
            throw new InvalidOrderException("Recipient name, phone number, province, and detailed address are required.");
        }
        if (!deliveryInfoRequest.getPhoneNumber().trim().matches("\\d{10}")) {
            throw new InvalidOrderException("Phone number must contain exactly 10 digits.");
        }

        String email = deliveryInfoRequest.getEmail();
        if (email != null && email.trim().isEmpty()) {
            email = null;
        }
        if (email != null && !email.trim().matches(EMAIL_PATTERN)) {
            throw new InvalidOrderException("Email address is invalid.");
        }

        String note = deliveryInfoRequest.getNote();
        return new ValidatedDeliveryInfo(
                deliveryInfoRequest.getRecipientName().trim(),
                deliveryInfoRequest.getPhoneNumber().trim(),
                email == null ? null : email.trim(),
                deliveryInfoRequest.getDeliveryProvince().trim(),
                deliveryInfoRequest.getDetailAddress().trim(),
                note == null || note.trim().isEmpty() ? null : note.trim());
    }
}
