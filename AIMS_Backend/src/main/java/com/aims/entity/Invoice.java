package com.aims.entity;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "invoice")
@Data
@Getter
@Setter
@NoArgsConstructor
public class Invoice {

    @Id
    @Column(name = "invoice_id")
    private String invoiceId;

    @Column(name = "issue_date")
    private LocalDateTime issueDate;

    @Column(name = "sub_total_ex_vat")
    private long subTotalExVAT;

    @Column(name = "sub_total_inc_vat")
    private long subTotalIncVAT;

    @Column(name = "shipping_fee")
    private long shippingFee;

    @Transient
    private long totalAmount;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "order_id", referencedColumnName = "order_id")
    private Order order;

    public Invoice(Order order) {
        this.invoiceId = UUID.randomUUID().toString();
        this.issueDate = LocalDateTime.now();
        this.order = order;
    }

    public long calculateTotalAmount() {
        this.totalAmount = this.subTotalIncVAT + this.shippingFee;
        return this.totalAmount;
    }

    /**
     * Alias method to satisfy invoice.getId() in PayPalController.
     */
    public String getId() {
        return this.invoiceId;
    }

    /**
     * Helper method to return the totalAmount as BigDecimal for currency
     * operations.
     */
    public BigDecimal getTotalAmount() {
        return BigDecimal.valueOf(this.totalAmount);
    }
}
