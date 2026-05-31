package com.aims.repository;

import com.aims.entity.Invoice;
import com.aims.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * JpaInvoiceRepository - Spring Data JPA repository for Invoice entity.
 */
@Repository
public interface JpaInvoiceRepository extends JpaRepository<Invoice, String> {
    Optional<Invoice> findByOrder(Order order);
}
