package com.aims.subsystem.vietqr.callback;

import java.util.List;
import java.util.function.Function;

import org.springframework.stereotype.Component;
import com.aims.repository.OrderRepository;
import com.aims.entity.Order;

/**
 * Resolves order IDs from transaction remarks content using pattern matching.
 */
@Component
public class OrderIdResolver {

    private final OrderRepository orderRepository;

    public OrderIdResolver(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Default parser chain — tried in order; first non-null result wins.
     * All parsers are JDK Function lambdas to keep the package lightweight.
     */
    private static final List<Function<String, String>> DEFAULT_CHAIN = List.of(

            // Format 1 — Legacy: "Order #ORD-001"
            content -> {
                int idx = content.indexOf("Order #");
                return idx >= 0 ? content.substring(idx + "Order #".length()).trim() : null;
            },

            // Format 2 — Current: "Order ORD001" (dash stripped by VietQRController)
            content -> content.startsWith("Order ")
                    ? restoreId(content.substring("Order ".length()).trim())
                    : null,

            // Format 3 — VietQR terminal prefix: "VQR<termCode> ORD001"
            content -> (content.startsWith("VQR") && content.contains(" "))
                    ? restoreId(content.substring(content.lastIndexOf(' ') + 1).trim())
                    : null,

            // Format 4 — Catch-all: content IS the orderId (e.g. "ORD001")
            content -> restoreId(content.trim())
    );

    /**
     * Resolves an orderId from the VietQR callback payload.
     *
     * <p>If {@code directOrderId} is present it is restored and returned
     * immediately without running the parser chain.</p>
     *
     * @param directOrderId orderId field sent directly in the VietQR payload (may be null/blank).
     * @param content       transfer content string to parse when directOrderId is absent.
     * @return the resolved, canonical orderId (e.g. {@code "ORD-001"}),
     *         or {@code null} if resolution fails.
     */
    public String resolve(String directOrderId, String content) {
        String candidate = null;
        if (directOrderId != null && !directOrderId.isBlank()) {
            candidate = restoreId(directOrderId.trim());
        } else if (content != null && !content.isBlank()) {
            String trimmed = content.trim();
            for (Function<String, String> parser : DEFAULT_CHAIN) {
                String result = parser.apply(trimmed);
                if (result != null && !result.isBlank()) {
                    candidate = result;
                    break;
                }
            }
        }

        if (candidate == null || candidate.isBlank()) {
            return null;
        }

        // If the candidate exists as a direct ID, return it immediately
        if (orderRepository.existsById(candidate)) {
            return candidate;
        }

        // Otherwise, attempt a suffix search in the database
        // Strip any hyphens from the candidate to match the database REPLACE
        String cleanCandidate = candidate.replace("-", "");
        List<Order> matching = orderRepository.findByOrderIdSuffix(cleanCandidate);
        if (matching.size() == 1) {
            return matching.get(0).getOrderId();
        } else if (matching.size() > 1) {
            System.out.println("[OrderIdResolver] Warning: Multiple orders match suffix " + cleanCandidate);
            return matching.get(0).getOrderId();
        }

        return candidate;
    }

    /**
     * Restores the canonical {@code ORD-001} format from a compact value
     * such as {@code ORD001} by inserting a dash between the letter prefix
     * and the numeric/alphanumeric suffix.
     *
     * <p>Examples: {@code "ORD001" → "ORD-001"}, {@code "ORD1K" → "ORD-1K"},
     * {@code "ORD-001" → "ORD-001"} (already has dash — returned as-is).</p>
     *
     * @param raw compact orderId; may be null.
     * @return canonical orderId with dash, or null if input is null.
     */
    private static String restoreId(String raw) {
        if (raw == null) return null;
        String trimmed = raw.trim();

        // Already has a dash — no restoration needed
        if (trimmed.contains("-")) return trimmed;

        // Split at first digit to find letter prefix and alphanumeric suffix
        int splitIdx = -1;
        for (int i = 0; i < trimmed.length(); i++) {
            if (Character.isDigit(trimmed.charAt(i))) {
                splitIdx = i;
                break;
            }
        }
        if (splitIdx > 0) {
            String prefix = trimmed.substring(0, splitIdx);
            String suffix = trimmed.substring(splitIdx);
            if (prefix.chars().allMatch(Character::isLetter) && !suffix.isEmpty()) {
                return prefix + "-" + suffix;
            }
        }
        return trimmed;
    }
}
