package com.aims.repository;

import com.aims.entity.OrderItem;
import com.aims.entity.OrderItemId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, OrderItemId> {

    @Query("SELECT oi FROM OrderItem oi JOIN FETCH oi.product WHERE oi.order.orderId = :orderId")
    List<OrderItem> findAllWithProductByOrderId(@Param("orderId") String orderId);
    List<OrderItem> findByOrderOrderId(String orderId);
}
