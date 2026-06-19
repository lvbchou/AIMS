package com.aims.service.product;

import com.aims.dto.product.ProductSummaryDTO;
import com.aims.mapper.product.ProductSummaryMapper;
import com.aims.repository.product.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.aims.exception.*;

@Service
@Transactional(readOnly = true)
public class ProductSearchService implements IProductSearchService {

    private final ProductRepository productRepository;
    private final ProductSummaryMapper productSummaryMapper;

    public ProductSearchService(ProductRepository productRepository,
                                ProductSummaryMapper productSummaryMapper) {
        this.productRepository = productRepository;
        this.productSummaryMapper = productSummaryMapper;
    }

    @Transactional(readOnly = true)
    public Page<ProductSummaryDTO> searchProduct(String keyword, String category, Pageable pageable) {
        isValidInput(keyword, category);
        return productRepository
                .searchAndFilter(keyword, category, 0L, Long.MAX_VALUE, pageable)
                .map(p -> productSummaryMapper.toDTO(p));
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
                .map(p -> productSummaryMapper.toDTO(p));
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

    private void isValidInput(String keyword, String category) {
        boolean keywordEmpty = (keyword == null || keyword.isBlank());
        boolean categoryEmpty = (category == null || category.isBlank());
        if (keywordEmpty && categoryEmpty) {
            throw new EmptySearchInputException();
        }
    }
}