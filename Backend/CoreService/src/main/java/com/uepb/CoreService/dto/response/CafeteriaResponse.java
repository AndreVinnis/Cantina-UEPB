package com.uepb.CoreService.dto.response;

public record CafeteriaResponse(
        String name,
        String email,
        Boolean active,
        String imageUrl
) {
}
