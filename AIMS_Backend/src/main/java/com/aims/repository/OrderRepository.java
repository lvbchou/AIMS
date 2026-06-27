package com.aims.repository;

import com.aims.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    @Query("SELECT o FROM Order o WHERE REPLACE(o.orderId, '-', '') LIKE %:suffix")
    List<Order> findByOrderIdSuffix(@Param("suffix") String suffix);
}
