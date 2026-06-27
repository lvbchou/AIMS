package com.aims.controller;

import com.aims.dto.stock.StockHistoryDTO;
import com.aims.service.StockService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stock")
@Tag(name = "Stock", description = "Adjust inventory and view stock history")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @PutMapping("/{productId}/adjust")
    public ResponseEntity<StockHistoryDTO> adjustStock(
            @PathVariable Integer productId,
            @RequestParam int quantityChange,
            @RequestParam String reason,
            @RequestParam(required = false) Integer userId) {
        return ResponseEntity.ok(
                stockService.adjustStock(productId, quantityChange, reason, userId));
    }

    @GetMapping("/{productId}/history")
    public ResponseEntity<List<StockHistoryDTO>> getHistory(
            @PathVariable Integer productId) {
        return ResponseEntity.ok(stockService.getHistoryByProduct(productId));
    }

    @GetMapping("/history")
    public ResponseEntity<List<StockHistoryDTO>> getAllHistory() {
        return ResponseEntity.ok(stockService.getAllHistory());
    }
}
