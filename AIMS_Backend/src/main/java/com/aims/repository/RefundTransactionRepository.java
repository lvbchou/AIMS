package com.aims.repository;

import com.aims.entity.RefundTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * RefundTransactionRepository - Spring Data JPA repository for RefundTransaction entity.
 */
@Repository
public interface RefundTransactionRepository extends JpaRepository<RefundTransaction, String> {
}
