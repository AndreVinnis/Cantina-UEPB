package com.uepb.CoreService.dto;

public record AuthRequest(
        String email,
        String password
) {
}
