package com.uepb.CoreService.dto.request;

import com.uepb.CoreService.enums.AvailabilityMode;
import com.uepb.CoreService.enums.Category;
import java.math.BigDecimal;

public record MenuItemRequest(
        String name,
        String description,
        BigDecimal price,
        Category category,
        AvailabilityMode availabilityMode,
        Integer stock
) {
}
