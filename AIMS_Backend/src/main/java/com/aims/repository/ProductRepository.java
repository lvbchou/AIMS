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

    // Dùng cho ViewProductDetails (productId lookup) - SD step 1.1.1
    @Query("SELECT p FROM Product p WHERE p.productId = :productId AND p.status = 'active'")
    Optional<Product> findActiveById(@Param("productId") Integer productId);

    // Dùng cho createProduct - check duplicate barcode
    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE p.barcode = :barcode")
    boolean existsByBarcode(@Param("barcode") String barcode);

    // Dùng cho getAllProducts (product list screen)
    @Query("SELECT p FROM Product p WHERE p.status = 'active'")
    List<Product> findAllActive();

    /**
     * SD SearchProduct step 1.1.3 + step 2.1.1 (kết hợp):
     * - Bước 1: searchProduct(keyword, category) → lọc theo title/category
     * - Bước 2 (opt): filterByPriceRange áp dụng TRÊN kết quả search
     *
     * Cả hai bước được thực hiện trong 1 query duy nhất để đúng với Sequence Diagram:
     * "filterProductsByPriceRange(productList, priceRange)" tức là filter
     * chỉ áp dụng trên tập đã search, không phải toàn bộ catalog.
     *
     * Nếu minPrice = 0 và maxPrice = Long.MAX_VALUE → không filter giá (chỉ search).
     * Nếu keyword = null và category = null → không áp dụng (dùng getAllProducts thay thế).
     */
    @Query("SELECT p FROM Product p WHERE p.status = 'active' " +
            "AND (:keyword IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:category IS NULL OR p.category = :category) " +
            "AND p.sellingPrice >= :minPrice AND p.sellingPrice <= :maxPrice")
    List<Product> searchAndFilter(
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("minPrice") long minPrice,
            @Param("maxPrice") long maxPrice);
}