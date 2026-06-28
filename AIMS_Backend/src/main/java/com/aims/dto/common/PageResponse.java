package com.aims.dto.common;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Cấu trúc phân trang "phẳng" để serialize JSON ổn định.
 * Từ Spring Boot 4 / Spring Data 4, serialize Page/PageImpl trực tiếp không còn
 * được hỗ trợ -> bọc qua DTO này. Giữ đúng shape FE đang đọc:
 * { content, totalElements, totalPages, number, size, first, last }.
 */
public record PageResponse<T>(
        List<T> content,
        long totalElements,
        int totalPages,
        int number,
        int size,
        boolean first,
        boolean last
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize(),
                page.isFirst(),
                page.isLast()
        );
    }
}