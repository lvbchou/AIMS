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
        * Search theo title HOẶC category (SD SearchProduct step 1.1.3).
        * Spec: "customers use product title or category to search".
        * - Cả hai đều LIKE + case-insensitive.
        * - keyword và category nhận CÙNG một chuỗi người dùng nhập → gõ trùng
        *   title hoặc trùng category đều ra kết quả.
        * - Nếu cả hai NULL → trả về toàn bộ (giữ tương thích getAll-style).
        */
@Query("SELECT p FROM Product p WHERE p.status = 'active' " +
        "AND ( (:keyword IS NULL AND :category IS NULL) " +
        "      OR (:keyword  IS NOT NULL AND LOWER(p.title)    LIKE LOWER(CONCAT('%', :keyword,  '%'))) " +
        "      OR (:category IS NOT NULL AND LOWER(p.category) LIKE LOWER(CONCAT('%', :category, '%'))) )")
Page<Product> searchByKeywordAndCategory(
        @Param("keyword") String keyword,
        @Param("category") String category,
        Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.status = 'active'")
    Page<Product> findAllActive(Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.productId = :productId")
    Optional<Product> findWithLockByProductId(Integer productId);

    @Query("SELECT p FROM Product p WHERE p.productId IN :ids")
    List<Product> findAllByIds(@Param("ids") List<Integer> ids);
}