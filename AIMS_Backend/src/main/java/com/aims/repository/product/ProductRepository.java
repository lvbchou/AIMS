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
     * Search thuần theo keyword + category (SD SearchProduct step 1.1.3).
     * Không phụ thuộc bất kỳ tham số giá nào → tuân thủ ISP.
     * Việc filter theo giá được thực hiện ở tầng service, áp trên tập đã search.
     */
    @Query("SELECT p FROM Product p WHERE p.status = 'active' " +
            "AND (:keyword IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:category IS NULL OR p.category = :category)")
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