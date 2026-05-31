package com.aims.repository;

import com.aims.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JpaOrderRepository - Spring Data JPA repository for Order entity.
 */
@Repository
public interface JpaOrderRepository extends JpaRepository<Order, String> {
}
