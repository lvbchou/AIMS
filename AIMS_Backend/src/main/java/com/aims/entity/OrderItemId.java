package com.aims.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class OrderItemId implements Serializable {

    @Column(name = "order_id", nullable = false, length = 45)
    private String orderId;

    @Column(name = "product_id", nullable = false)
    private Integer productId;
}
