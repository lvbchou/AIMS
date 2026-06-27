package com.aims.entity;

import com.aims.entity.product.Product;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Line items for order invoice screen (UC003 Table 2).
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "order_item")
@IdClass(OrderItemId.class)
@Data
public class OrderItem {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false)
    private int quantity;
}
