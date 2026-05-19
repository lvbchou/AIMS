package com.aims.repository;

import com.aims.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    @Query(value = "SELECT * FROM aims.product WHERE barcode = :barcode",
            nativeQuery = true)
    Optional<Product> findByBarcode(@Param("barcode") String barcode);

    @Query(value = "SELECT COUNT(*) > 0 FROM aims.product WHERE barcode = :barcode",
            nativeQuery = true)
    boolean existsByBarcode(@Param("barcode") String barcode);

    @Query(value = """
            SELECT * FROM aims.product
            WHERE (:keyword IS NULL OR LOWER(title) LIKE LOWER(CONCAT('%', :keyword, '%')))
            AND   (:category IS NULL OR category = :category)
            """, nativeQuery = true)
    List<Product> searchByKeywordAndCategory(
            @Param("keyword") String keyword,
            @Param("category") String category);

    @Query(value = """
            SELECT * FROM aims.product
            WHERE selling_price >= :minPrice
            AND   selling_price <= :maxPrice
            """, nativeQuery = true)
    List<Product> findByPriceRange(
            @Param("minPrice") long minPrice,
            @Param("maxPrice") long maxPrice
    );
}