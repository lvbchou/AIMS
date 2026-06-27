package com.aims.entity;

import com.aims.exception.EmptyCartException;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity for {@code aims.orders}. Used by UC003 Pay Order.
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@Data
public class Order {

    @Id
    @Column(name = "order_id", length = 45)
    private String orderId;

    @Column(name = "status", nullable = false, length = 45)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "payment_method", length = 45)
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Transient
    private Cart cart;

    @Transient
    private DeliveryInfo deliveryInfor;

    @PrePersist
    protected void onCreate() {
        if (this.orderId == null) {
            this.orderId = UUID.randomUUID().toString();
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = "pending";
        }
    }

    public Order(Cart cart) {
        if (cart == null || cart.isEmpty()) {
            throw new EmptyCartException("Cannot create an order with an empty cart.");
        }
        this.orderId = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.cart = cart;
        this.status = "pending";
    }
}
