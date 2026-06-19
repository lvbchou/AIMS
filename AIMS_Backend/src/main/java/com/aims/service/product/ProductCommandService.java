// service/command/ProductCommandService.java
package com.aims.service.product;

import com.aims.dto.product.ProductInfoDTO;
import com.aims.entity.product.Product;
import com.aims.exception.ProductAlreadyExistsException;
import com.aims.exception.ProductNotFoundException;
import com.aims.repository.product.ProductRepository;
import com.aims.service.product.creator.ProductCreatorRegistry;
import com.aims.service.product.updater.ProductUpdaterRegistry;
import com.aims.service.product.validator.ProductValidatorRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class ProductCommandService implements IProductCommandService {

    private final ProductRepository productRepository;
    private final ProductValidatorRegistry validatorRegistry;
    private final ProductCreatorRegistry creatorRegistry;
    private final ProductUpdaterRegistry updaterRegistry;

    public ProductCommandService(ProductRepository productRepository,
                                 ProductValidatorRegistry validatorRegistry,
                                 ProductCreatorRegistry creatorRegistry,
                                 ProductUpdaterRegistry updaterRegistry) {
        this.productRepository = productRepository;
        this.validatorRegistry = validatorRegistry;
        this.creatorRegistry = creatorRegistry;
        this.updaterRegistry = updaterRegistry;
    }

    public Product saveProduct(ProductInfoDTO dto) {
        validatorRegistry.getValidator(dto.getProductType()).validate(dto);

        if (productRepository.existsByBarcode(dto.getBarcode())) {
            throw new ProductAlreadyExistsException(dto.getBarcode());
        }

        Product product = creatorRegistry.getCreator(dto.getProductType()).create(dto);
        return productRepository.save(product);
    }

    public void updateProduct(Integer productId, ProductInfoDTO dto) {
        validatorRegistry.getValidator(dto.getProductType()).validate(dto);
        Product existing = productRepository.findActiveById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        if (productRepository.existsByBarcode(dto.getBarcode()) && !dto.getBarcode().equals(existing.getBarcode())) {
            throw new ProductAlreadyExistsException(dto.getBarcode());
        }
        updaterRegistry.getUpdater(dto.getProductType()).update(existing, dto);
        productRepository.save(existing);
    }

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
}