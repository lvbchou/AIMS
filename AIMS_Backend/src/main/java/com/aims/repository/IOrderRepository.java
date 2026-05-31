package com.aims.repository;

import com.aims.entity.Order;

/**
 * IOrderRepository - interface for Order persistence operations.
 */
public interface IOrderRepository {
    void updateOrder(Order order);
    Order findByToken(String token);
}
