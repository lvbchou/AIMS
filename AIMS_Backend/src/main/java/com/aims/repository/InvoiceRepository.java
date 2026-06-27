package com.aims.repository;

import com.aims.entity.Invoice;
import com.aims.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, String>, IInvoiceRepository {

    Optional<Invoice> findByOrderOrderId(String orderId);

    /**
     * Satisfy IInvoiceRepository.findByOrder — delegates to JpaInvoiceRepository equivalent.
     * Note: this method is declared in JpaInvoiceRepository; here we add it to the unified
     * InvoiceRepository so that a single injection point covers all invoice queries.
     */
    Optional<Invoice> findByOrder(Order order);
}
