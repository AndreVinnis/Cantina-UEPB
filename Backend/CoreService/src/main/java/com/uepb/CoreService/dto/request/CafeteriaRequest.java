package com.uepb.CoreService.dto.request;

import com.uepb.CoreService.enums.Campus;

public record CafeteriaRequest (
    String name,
    String email,
    String password,
    Campus campus
) {
}
