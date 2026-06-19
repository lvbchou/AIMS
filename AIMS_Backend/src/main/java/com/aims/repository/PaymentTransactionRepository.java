package com.aims.repository;

import com.aims.entity.PaymentTransaction;
import com.aims.entity.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, String> {

    boolean existsByInvoiceIdAndStatus(String invoiceId, TransactionStatus status);

    Optional<PaymentTransaction> findFirstByInvoiceIdAndStatusOrderByTransactionTimeDesc(
            String invoiceId,
            TransactionStatus status);
}
