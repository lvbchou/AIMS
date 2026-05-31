package com.aims.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Shipping contact snapshot linked to {@code aims.orders}.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "delivery")
@Data
public class Delivery {

    @Id
    @Column(name = "order_id", length = 45)
    private String orderId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    private Order order;

    @Column(name = "recipient_name", nullable = false, length = 255)
    private String recipientName;

    @Column(name = "phone_number", nullable = false, length = 45)
    private String phoneNumber;

    @Column(name = "email")
    private String email;

    @Column(name = "delivery_province", nullable = false, length = 45)
    private String deliveryProvince;

    @Column(name = "detail_address", nullable = false)
    private String detailAddress;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;
}
