package com.aims.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_history")
@Getter
@Setter
@NoArgsConstructor
public class ProductHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private int historyId;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "product_id")
    private Integer productId;

    @Column(name = "action", nullable = false)
    private String action; // "CREATE", "UPDATE", "DELETE"

    @Column(name = "time", nullable = false)
    private LocalDateTime time;

    public ProductHistory(Integer userId, Integer productId, String action) {
        this.userId    = userId;
        this.productId = productId;
        this.action    = action;
        this.time      = LocalDateTime.now();
    }
}