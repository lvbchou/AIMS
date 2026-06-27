package com.aims.repository;

import com.aims.entity.Invoice;
import com.aims.entity.Order;

import java.util.Optional;

/**
 * IInvoiceRepository — unified abstraction for Invoice persistence operations.
 *
 * <h3>Design Rationale</h3>
 * <p><strong>DIP (Dependency Inversion Principle):</strong> The service layer
 * previously injected both {@link InvoiceRepository} (which has
 * {@code findByOrderOrderId}) and {@link JpaInvoiceRepository} (which has
 * {@code findByOrder}) as separate concrete JPA classes.  This violated DIP —
 * the service knew about the persistence technology.</p>
 *
 * <p>This interface unifies both query methods behind a single abstraction.
 * The service depends on this interface; the underlying JPA repositories
 * implement it.</p>
 *
 * <p><strong>ISP (Interface Segregation Principle):</strong> The interface
 * exposes only the methods the payment service actually uses.</p>
 */
public interface IInvoiceRepository {

    /**
     * Finds an invoice by the associated order's ID.
     *
     * @param orderId the order identifier.
     * @return an {@link Optional} containing the invoice, or empty if not found.
     */
    Optional<Invoice> findByOrderOrderId(String orderId);

    /**
     * Finds an invoice by the associated {@link Order} entity.
     *
     * @param order the order entity.
     * @return an {@link Optional} containing the invoice, or empty if not found.
     */
    Optional<Invoice> findByOrder(Order order);

    // Note: save(Invoice) is intentionally omitted — callers use the save() method
    // inherited from JpaRepository (via InvoiceRepository) which resolves without ambiguity.
}
