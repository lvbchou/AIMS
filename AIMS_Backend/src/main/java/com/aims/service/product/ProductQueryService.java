package com.aims.service.product;

import com.aims.dto.product.ProductInfoDTO;
import com.aims.dto.product.ProductSummaryDTO;
import com.aims.entity.product.Product;
import com.aims.exception.ProductNotFoundException;
import com.aims.mapper.product.ProductMapper;
import com.aims.mapper.product.ProductMapperRegistry;
import com.aims.mapper.product.ProductSummaryMapper;
import com.aims.repository.product.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProductQueryService implements IProductQueryService {

    private final ProductRepository productRepository;
    private final ProductMapperRegistry mapperRegistry;
    private final ProductSummaryMapper productSummaryMapper;

    public ProductQueryService(ProductRepository productRepository,
                               ProductMapperRegistry mapperRegistry,
                               ProductSummaryMapper productSummaryMapper) {
        this.productRepository = productRepository;
        this.mapperRegistry = mapperRegistry;
        this.productSummaryMapper = productSummaryMapper;
    }

    @Transactional(readOnly = true)
    public Page<ProductSummaryDTO> getAllProducts(Pageable pageable) {
        return productRepository.findAllActive(pageable)
                .map(p -> productSummaryMapper.toDTO(p));
    }

    public List<ProductSummaryDTO> getProductsByIds(List<Integer> ids) {
        return productRepository.findAllById(ids)
                .stream()
                .map(p -> productSummaryMapper.toDTO(p))
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductInfoDTO viewProduct(Integer productId) {
        Product product = productRepository.findActiveById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        return mapperRegistry.getMapper(product).toDTO(product);
    }
}
