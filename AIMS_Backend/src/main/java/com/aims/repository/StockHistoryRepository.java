package com.aims.repository;

import com.aims.entity.StockHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockHistoryRepository extends JpaRepository<StockHistory, Integer> {

    @Query("SELECT h FROM StockHistory h WHERE h.productId = :productId ORDER BY h.changeTime DESC")
    List<StockHistory> findByProductId(@Param("productId") Integer productId);

    @Query("SELECT h FROM StockHistory h ORDER BY h.changeTime DESC")
    List<StockHistory> findAllOrderByTimeDesc();
}
