package com.uepb.CoreService.dto.response;

import com.uepb.CoreService.enums.AvailabilityMode;
import com.uepb.CoreService.enums.Category;
import java.math.BigDecimal;
import java.time.Instant;

public record MenuItemResponse(
        String cafeteria_id,
        String name,
        String description,
        BigDecimal price,
        Boolean availability,
        Category category,
        AvailabilityMode availabilityMode,
        Integer stock,
        Instant createdAt
) {
}
