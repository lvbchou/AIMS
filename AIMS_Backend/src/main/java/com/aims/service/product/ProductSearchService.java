package com.aims.service.product;

import com.aims.dto.product.ProductSummaryDTO;
import com.aims.entity.product.Product;
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
    private final PriceRangeParser priceRangeParser;

    public ProductSearchService(ProductRepository productRepository,
                                ProductSummaryMapper productSummaryMapper,
                                PriceRangeParser priceRangeParser) {
        this.productRepository = productRepository;
        this.productSummaryMapper = productSummaryMapper;
        this.priceRangeParser = priceRangeParser;
    }

    @Transactional(readOnly = true)
    public Page<ProductSummaryDTO> searchProduct(String keyword, String category, Pageable pageable) {
        isValidInput(keyword, category);
        return productRepository
                .searchByKeywordAndCategory(keyword, category, pageable)
                .map(p -> productSummaryMapper.toDTO(p));
    }

    @Transactional(readOnly = true)
    public Page<ProductSummaryDTO> filterProductsByPriceRange(
            String keyword, String category, String priceRange, Pageable pageable) {

        isValidInput(keyword, category);
        long[] range = priceRangeParser.parse(priceRange);
        long min = range[0];
        long max = range[1];

        // Filter giá ngay tại DB; DB tự phân trang (đúng SOLID + scale tốt)
        return productRepository
                .searchByKeywordCategoryAndPriceRange(keyword, category, min, max, pageable)
                .map(productSummaryMapper::toDTO);
    }

    private void isValidInput(String keyword, String category) {
        boolean keywordEmpty = (keyword == null || keyword.isBlank());
        boolean categoryEmpty = (category == null || category.isBlank());
        if (keywordEmpty && categoryEmpty) {
            throw new EmptySearchInputException();
        }
    }
}