/**
 * ProductMapper
 *
 * Cohesion Level: Functional
 * Reason: Single class, single method, single purpose —
 *   map a Product entity to ProductInfoDTO.
 *
 * Coupling:
 *   - Content coupling with Book, CD, DVD, Newspaper:
 *     directly reads internal data of each subtype via getters
 *     after downcasting (getAuthor, getTracks, etc.).
 *     Improvement: add abstract method toDTO() to Product,
 *     each subtype maps its own data — ProductMapper simply calls product.toDTO().
 */
package com.aims.mapper;

import com.aims.dto.product.ProductInfoDTO;
import com.aims.entity.product.Product;

public class ProductMapper {

    public static ProductInfoDTO toDTO(Product product) {
        return product.toDTO();
    }
}