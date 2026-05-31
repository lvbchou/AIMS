package com.aims.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_history")
@Getter
@Setter
@NoArgsConstructor
public class StockHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "change_id", nullable = false)
    private Integer changeId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @Column(name = "quantity_change", nullable = false)
    private Integer quantityChange;  // dương = tăng, âm = giảm

    @Column(name = "old_quantity", nullable = false)
    private Integer oldQuantity;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Column(name = "change_time", nullable = false)
    private LocalDateTime changeTime;

    public StockHistory(Integer userId, Integer productId,
                        int quantityChange, int oldQuantity, String reason) {
        this.userId         = userId;
        this.productId      = productId;
        this.quantityChange = quantityChange;
        this.oldQuantity    = oldQuantity;
        this.reason         = reason;
        this.changeTime     = LocalDateTime.now();
    }
}