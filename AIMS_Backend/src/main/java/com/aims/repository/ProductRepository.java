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

    // Dùng cho ViewProductDetails (barcode lookup)
    @Query("SELECT p FROM Product p WHERE p.barcode = :barcode AND p.status = 'active'")
    Optional<Product> findByBarcode(@Param("barcode") String barcode);

    // Dùng cho ViewProductDetails (productId lookup) - SD step 1.1.1
    @Query("SELECT p FROM Product p WHERE p.productId = :productId AND p.status = 'active'")
    Optional<Product> findActiveById(@Param("productId") Integer productId);

    // Dùng cho createProduct - check duplicate barcode
    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE p.barcode = :barcode")
    boolean existsByBarcode(@Param("barcode") String barcode);

    // Dùng cho SearchProduct - SD step 1.1.3
    @Query("SELECT p FROM Product p WHERE p.status = 'active' " +
            "AND (:keyword IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:category IS NULL OR p.category = :category)")
    List<Product> searchByKeywordAndCategory(
            @Param("keyword") String keyword,
            @Param("category") String category);

    // Dùng cho getAllProducts (product list screen)
    @Query("SELECT p FROM Product p WHERE p.status = 'active'")
    List<Product> findAllActive();

    // Dùng cho FilterProduct - SD step 2.1.1
    @Query("SELECT p FROM Product p WHERE p.status = 'active' " +
            "AND p.sellingPrice >= :minPrice AND p.sellingPrice <= :maxPrice")
    List<Product> findByPriceRange(
            @Param("minPrice") long minPrice,
            @Param("maxPrice") long maxPrice);
}