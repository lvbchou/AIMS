package com.aims.repository;

import com.aims.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.*;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    @Query("SELECT p FROM Product p WHERE p.barcode = :barcode AND p.status = 'active'")
    Optional<Product> findByBarcode(@Param("barcode") String barcode);

    @Query("SELECT p FROM Product p WHERE p.productId = :productId AND p.status = 'active'")
    Optional<Product> findActiveById(@Param("productId") Integer productId);

    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE barcode = :barcode")
    boolean existsByBarcode(@Param("barcode") String barcode);

    @Query("SELECT p FROM Product p WHERE p.status = 'active' " +
            "AND (:keyword IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:category IS NULL OR p.category = :category)")
    List<Product> searchByKeywordAndCategory(
            @Param("keyword") String keyword,
            @Param("category") String category);

    @Query("SELECT p FROM Product p WHERE p.status = 'active'")
    List<Product> findAllActive();
    @Query(value = "SELECT * FROM aims.product WHERE barcode = :barcode", nativeQuery = true)
    Optional<Product> findByBarcode(@Param("barcode") String barcode);

    @Query(value = "SELECT COUNT(*) > 0 FROM aims.product WHERE barcode = :barcode", nativeQuery = true)
    boolean existsByBarcode(@Param("barcode") String barcode);

    /**
     * Search products where keyword matches title OR category (case-insensitive, partial match).
     * Per problem statement: customers use product title or category to search.
     * Category values: Book, CD, DVD, Newspaper.
     */
    @Query(value = """
            SELECT * FROM aims.product
            WHERE LOWER(title)    LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR    LOWER(category) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """, nativeQuery = true)
    List<Product> searchByKeywordOrCategory(@Param("keyword") String keyword);

    @Query(value = """
            SELECT * FROM aims.product
            WHERE selling_price >= :minPrice AND selling_price <= :maxPrice
            """, nativeQuery = true)
    List<Product> findByPriceRange(@Param("minPrice") long minPrice, @Param("maxPrice") long maxPrice);
}