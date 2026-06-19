package com.aims.service.product;

import com.aims.exception.InvalidProductInfoException;
import org.springframework.stereotype.Component;

@Component
public class PriceRangeParser {

    /**
     * Parse chuỗi "min-max" thành mảng [min, max].
     * Ví dụ hợp lệ: "100000-200000"
     */
    public long[] parse(String priceRange) {
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
}