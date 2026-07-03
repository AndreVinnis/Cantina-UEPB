package com.uepb.CoreService.dto.request;

public record AuthRequest(
        String email,
        String password
) {
}
