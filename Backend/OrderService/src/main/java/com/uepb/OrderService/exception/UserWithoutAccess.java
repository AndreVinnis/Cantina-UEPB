package com.uepb.OrderService.exception;

public class UserWithoutAccess extends RuntimeException {
    public UserWithoutAccess(String message) {
        super(message);
    }
}
