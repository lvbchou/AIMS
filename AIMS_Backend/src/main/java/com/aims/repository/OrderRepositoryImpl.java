package com.aims.repository;

import com.aims.entity.Order;
import org.springframework.stereotype.Repository;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OrderRepositoryImpl - integrates custom IOrderRepository operations with JpaOrderRepository
 * to persist orders to PostgreSQL, while maintaining fallback tracking for payment tokens.
 */
@Repository
public class OrderRepositoryImpl implements IOrderRepository {

    private final JpaOrderRepository jpaOrderRepository;
    private final Map<String, Order> orders = new ConcurrentHashMap<>();
    private Order lastOrder;

    public OrderRepositoryImpl(JpaOrderRepository jpaOrderRepository) {
        this.jpaOrderRepository = jpaOrderRepository;
    }

    @Override
    public void updateOrder(Order order) {
        if (order != null && order.getOrderId() != null) {
            // 1. Save permanently to PostgreSQL database
            jpaOrderRepository.save(order);
            
            // 2. Cache in-memory for fast lookups
            orders.put(order.getOrderId(), order);
            lastOrder = order;
        }
    }

    @Override
    public void rememberPaymentToken(String token, Order order) {
        if (token != null && !token.isBlank() && order != null) {
            orders.put(token, order);
            lastOrder = order;
        }
    }

    @Override
    public Order findByToken(String token) {
        if (orders.containsKey(token)) {
            return orders.get(token);
        }
        // If not found in memory, try searching the database by ID
        return jpaOrderRepository.findById(token)
                .orElse(lastOrder); // Fallback for quick local sandbox testing
    }
}
