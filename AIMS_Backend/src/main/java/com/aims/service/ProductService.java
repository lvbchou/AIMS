/**
 * ProductService
 *
 * Cohesion Level: Procedural
 * Reason: Methods validateProductInfo(), buildProductFromDTO(), and
 *   repository.save() are grouped because they form an ordered sequence
 *   of steps in the save/update flow — validate first, build next, then persist.
 *   Each step is a precondition for the next, which is the characteristic
 *   of procedural cohesion.
 *
 * Coupling:
 *   - Data coupling with ProductRepository: passes only primitives
 *     or typed entities, no internal state of repository is accessed.
 *   - Content coupling with Book, CD, DVD, Newspaper (updateProduct):
 *     directly modifies internal data of each subtype via setters after downcasting.
 *     Improvement: add abstract method applyUpdate(ProductInfoDTO dto) to Product.
 *   - Control coupling with ProductInfoDTO (validateProductInfo, buildProductFromDTO):
 *     productType string flag controls which branch of logic executes.
 *     Improvement: extract per-type validators and factories selected via a map.
 */
/*
    Coupling level: Data coupling with ProductRepository.
        - Reason: findActiveById(Integer productId) and searchAndFilter(keyword, category,
                  minPrice, maxPrice) pass only the exact parameters needed. The repository
                  does not depend on service logic, and the service does not access
                  repository internals. Good data coupling.
 */

package com.aims.service;

import com.aims.dto.product.ProductInfoDTO;
import com.aims.dto.product.ProductSummaryDTO;
import com.aims.entity.product.Product;
import com.aims.exception.*;
import com.aims.mapper.ProductMapper;
import com.aims.repository.ProductRepository;
import com.aims.service.creator.ProductCreatorRegistry;
import com.aims.service.validator.ProductValidatorRegistry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductValidatorRegistry validatorRegistry;
    private final ProductCreatorRegistry creatorRegistry;

    public ProductService(ProductRepository productRepository,
                          ProductValidatorRegistry validatorRegistry,
                          ProductCreatorRegistry creatorRegistry) {
        this.productRepository = productRepository;
        this.validatorRegistry = validatorRegistry;
        this.creatorRegistry = creatorRegistry;
    }

    // CREATE PRODUCT
    public Product saveProduct(ProductInfoDTO productInfo) {
        validatorRegistry.getValidator(productInfo.getProductType()).validate(productInfo);

        if (productRepository.existsByBarcode(productInfo.getBarcode())) {
            throw new ProductAlreadyExistsException(productInfo.getBarcode());
        }

        Product product = creatorRegistry.getFactory(productInfo.getProductType()).create(productInfo);
        return productRepository.save(product);
    }

    // UPDATE PRODUCT
    public void updateProduct(Integer productId, ProductInfoDTO dto) {
        validatorRegistry.getValidator(dto.getProductType()).validate(dto);
        Product existing = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        existing.applyCommonUpdate(dto);
        existing.applyUpdate(dto);
        productRepository.save(existing);
    }

    // DELETE PRODUCT
    public void deleteProduct(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        if (product.getQuantityInStock() > 0) {
            product.setStatus("deactivated");
        } else {
            product.setStatus("deleted");
        }
        productRepository.save(product);
    }

    public void deleteManyProducts(List<Integer> productIds) {
        for (Integer id : productIds) {
            productRepository.findById(id)
                    .ifPresent(product -> deleteProduct(product.getProductId()));
        }
    }

    // VIEW PRODUCT DETAILS
    @Transactional(readOnly = true)
    public Page<ProductSummaryDTO> getAllProducts(Pageable pageable) {
        return productRepository.findAllActive(pageable)
                .map(p -> new ProductSummaryDTO(
                        p.getProductId(),
                        p.getTitle(),
                        p.getClass().getSimpleName().toUpperCase(), // "CD", "DVD"...
                        p.getSellingPrice(),
                        p.getImage()));
    }

    @Transactional(readOnly = true)
    public ProductInfoDTO viewProduct(Integer productId) {
        Product product = productRepository.findActiveById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        return ProductMapper.toDTO(product);
    }

    // SEARCH AND FILTER PRODUCTS
    private void isValidInput(String keyword, String category) {
        boolean keywordEmpty = (keyword == null || keyword.isBlank());
        boolean categoryEmpty = (category == null || category.isBlank());
        if (keywordEmpty && categoryEmpty) {
            throw new EmptySearchInputException();
        }
    }

    @Transactional(readOnly = true)
    public Page<ProductSummaryDTO> searchProduct(String keyword, String category, Pageable pageable) {
        isValidInput(keyword, category);

        return productRepository
                .searchAndFilter(keyword, category, 0L, Long.MAX_VALUE, pageable)
                .map(p -> new ProductSummaryDTO(
                        p.getProductId(),
                        p.getTitle(),
                        p.getClass().getSimpleName().toUpperCase(),
                        p.getSellingPrice(),
                        p.getImage()));
    }

    @Transactional(readOnly = true)
    public Page<ProductSummaryDTO> filterProductsByPriceRange(
            String keyword, String category, String priceRange, Pageable pageable) {

        long[] range = parsePriceRange(priceRange);

        boolean noSearchContext = (keyword == null || keyword.isBlank())
                && (category == null || category.isBlank());

        return productRepository
                .searchAndFilter(
                        noSearchContext ? null : keyword,
                        noSearchContext ? null : category,
                        range[0],
                        range[1],
                        pageable)
                .map(p -> new ProductSummaryDTO(
                        p.getProductId(),
                        p.getTitle(),
                        p.getClass().getSimpleName().toUpperCase(),
                        p.getSellingPrice(),
                        p.getImage()));
    }

    private long[] parsePriceRange(String priceRange) {
        String[] parts = priceRange.split("-");
        if (parts.length != 2) {
            throw new InvalidProductInfoException(
                    "Invalid price range format. Expected: min-max (e.g. 100000-200000)");
        }
        try {
            long min = Long.parseLong(parts[0].trim());
            long max = Long.parseLong(parts[1].trim());
            if (min < 0 || max < min) {
                throw new InvalidProductInfoException(
                        "Price range invalid: min must be >= 0 and max >= min");
            }
            return new long[] { min, max };
        } catch (NumberFormatException e) {
            throw new InvalidProductInfoException(
                    "Price range must contain valid numbers. Expected: min-max");
        }
    }

    // STOCK UTILITIES
    @Transactional(readOnly = true)
    public boolean validateQuantityOfSelectedProducts() {
        return productRepository.findAll()
                .stream()
                .allMatch(p -> p.getQuantityInStock() >= 0);
    }

    @Transactional(readOnly = true)
    public boolean checkStockAvailable(Integer productId) {
        Product product = productRepository.findActiveById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        return product.getQuantityInStock() > 0;
    }
}