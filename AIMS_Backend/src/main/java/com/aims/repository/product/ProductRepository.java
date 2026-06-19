/**
 * ISP VIOLATION (searchAndFilter method):
 * Callers that only need keyword search must still supply minPrice=0 and
 * maxPrice=Long.MAX_VALUE — parameters they do not conceptually need.
 * They are forced to depend on price-filter parameters irrelevant to their use case.
 *
 * Impact: Forces callers to pass dummy values, making the API confusing and
 * harder to use correctly. Increases unnecessary coupling between search
 * and filter concerns.
 *
 * Improvement: Split into searchByKeywordAndCategory(keyword, category, pageable)
 * and filterByPriceRange(min, max, pageable). Compose them at the service layer
 * only when both dimensions are required simultaneously.
 */
package com.aims.repository.product;

import com.aims.entity.product.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    @Query("SELECT p FROM Product p WHERE p.barcode = :barcode AND p.status = 'active'")
    Optional<Product> findByBarcode(@Param("barcode") String barcode);

    @Query("SELECT p FROM Product p WHERE p.productId = :productId AND p.status = 'active'")
    Optional<Product> findActiveById(@Param("productId") Integer productId);

    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE barcode = :barcode")
    boolean existsByBarcode(@Param("barcode") String barcode);

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
    Page<Product> searchAndFilter(
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("minPrice") long minPrice,
            @Param("maxPrice") long maxPrice,
            Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.status = 'active'")
    Page<Product> findAllActive(Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.productId = :productId")
    Optional<Product> findWithLockByProductId(Integer productId);

    @Query("SELECT p FROM Product p WHERE p.productId IN :ids")
    List<Product> findAllByIds(@Param("ids") List<Integer> ids);
}