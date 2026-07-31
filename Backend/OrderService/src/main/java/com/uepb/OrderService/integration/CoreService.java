package com.uepb.OrderService.integration;

import com.uepb.OrderService.dto.request.OrderItemRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "CoreService", fallbackFactory = CoreServiceFallbackFactory.class)
public interface CoreService {

    @PostMapping("/cafeteria/internal/{id}/items/validate")
    List<ItemValidatedResponse> validateOrderItems(
            @PathVariable("id") String id,
            @RequestBody List<OrderItemRequest> orderRequest
    );

    @PostMapping("/cafeteria/internal/{id}/items/decrements")
    Void decrementsStock(
            @PathVariable("id") String id,
            @RequestBody List<OrderItemRequest> orderRequest
    );

    @GetMapping("/cafeteria/internal/id")
    String getIdCafeteria();
}
