package com.aims.repository;

import com.aims.entity.ProductHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductHistoryRepository extends JpaRepository<ProductHistory, Integer> {

    @Query("SELECT h FROM ProductHistory h ORDER BY h.time DESC")
    List<ProductHistory> findAllOrderByTimeDesc();
}
