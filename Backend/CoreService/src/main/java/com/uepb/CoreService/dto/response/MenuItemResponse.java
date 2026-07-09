package com.uepb.CoreService.dto.response;

import com.uepb.CoreService.enums.Category;
import java.math.BigDecimal;
import java.time.Instant;

public record MenuItemResponse(
        String cafeteriaName,
        String name,
        String description,
        BigDecimal price,
        Category category,
        Integer stock
) {
}
