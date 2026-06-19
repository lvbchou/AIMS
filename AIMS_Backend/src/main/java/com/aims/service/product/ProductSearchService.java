package com.aims.service.product;

import com.aims.dto.product.ProductSummaryDTO;
import com.aims.entity.product.Product;
import com.aims.mapper.product.ProductSummaryMapper;
import com.aims.repository.product.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.aims.exception.*;

import java.util.List;

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

        // Bước 1: search ra tập kết quả (đúng SD: filter áp TRÊN tập đã search)
        List<Product> searched = productRepository
                .searchByKeywordAndCategory(keyword, category, Pageable.unpaged())
                .getContent();

        // Bước 2: filter giá trên chính tập đã search (in-memory)
        List<ProductSummaryDTO> filtered = searched.stream()
                .filter(p -> p.getSellingPrice() >= min && p.getSellingPrice() <= max)
                .map(productSummaryMapper::toDTO)
                .toList();

        // Bước 3: phân trang thủ công trên kết quả đã filter
        return paginate(filtered, pageable);
    }

    private Page<ProductSummaryDTO> paginate(List<ProductSummaryDTO> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        if (start >= list.size()) {
            return new PageImpl<>(List.of(), pageable, list.size());
        }
        int end = Math.min(start + pageable.getPageSize(), list.size());
        return new PageImpl<>(list.subList(start, end), pageable, list.size());
    }

    private void isValidInput(String keyword, String category) {
        boolean keywordEmpty = (keyword == null || keyword.isBlank());
        boolean categoryEmpty = (category == null || category.isBlank());
        if (keywordEmpty && categoryEmpty) {
            throw new EmptySearchInputException();
        }
    }
}