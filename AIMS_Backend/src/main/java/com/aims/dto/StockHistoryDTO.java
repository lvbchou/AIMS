package com.aims.dto;

import java.time.LocalDateTime;

public record StockHistoryDTO(
        Integer changeId,
        Integer userId,
        Integer productId,
        Integer quantityChange,
        Integer oldQuantity,
        String reason,
        LocalDateTime changeTime
) {}
