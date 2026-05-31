package com.aims.service;

import com.aims.dto.StockHistoryDTO;
import com.aims.entity.StockHistory;
import com.aims.entity.product.Product;
import com.aims.exception.InvalidProductInfoException;
import com.aims.exception.ProductNotFoundException;
import com.aims.repository.ProductRepository;
import com.aims.repository.StockHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class StockService {

    private final ProductRepository productRepository;
    private final StockHistoryRepository historyRepository;

    public StockService(ProductRepository productRepository,
                        StockHistoryRepository historyRepository) {
        this.productRepository  = productRepository;
        this.historyRepository  = historyRepository;
    }

    public StockHistoryDTO adjustStock(Integer productId, int quantityChange,
                                       String reason, Integer userId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        int oldQuantity = product.getQuantityInStock();
        int newQuantity = oldQuantity + quantityChange;

        if (newQuantity < 0) {
            throw new InvalidProductInfoException(
                    "Stock cannot be negative. Current: " + oldQuantity);
        }

        product.setQuantityInStock(newQuantity);
        productRepository.save(product);

        StockHistory history = new StockHistory(
                userId, productId, quantityChange, oldQuantity, reason);
        historyRepository.save(history);

        return new StockHistoryDTO(
                history.getChangeId(),
                userId,
                productId,
                quantityChange,
                oldQuantity,
                reason,
                history.getChangeTime()
        );
    }

    @Transactional(readOnly = true)
    public List<StockHistoryDTO> getHistoryByProduct(Integer productId) {
        return historyRepository.findByProductId(productId).stream()
                .map(h -> new StockHistoryDTO(
                        h.getChangeId(),
                        h.getUserId(),
                        h.getProductId(),  // title load riêng nếu cần
                        h.getQuantityChange(),
                        h.getOldQuantity(),
                        h.getReason(),
                        h.getChangeTime()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StockHistoryDTO> getAllHistory() {
        return historyRepository.findAllOrderByTimeDesc().stream()
                .map(h -> new StockHistoryDTO(
                        h.getChangeId(),
                        h.getUserId(),
                        h.getProductId(),
                        h.getQuantityChange(),
                        h.getOldQuantity(),
                        h.getReason(),
                        h.getChangeTime()
                ))
                .toList();
    }
}
