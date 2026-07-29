package com.uepb.OrderService.service;

import com.uepb.OrderService.domain.Order;
import com.uepb.OrderService.domain.OrderItem;
import com.uepb.OrderService.dto.events.OrderCreatedEvent;
import com.uepb.OrderService.dto.request.OrderRequest;
import com.uepb.OrderService.dto.response.ClientOrderResponse;
import com.uepb.OrderService.dto.response.OrderItemResponse;
import com.uepb.OrderService.enums.PaymentMethod;
import com.uepb.OrderService.enums.Status;
import com.uepb.OrderService.integration.CoreService;
import com.uepb.OrderService.integration.ItemValidatedResponse;
import com.uepb.OrderService.repository.OrderItemRepository;
import com.uepb.OrderService.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CoreService coreService;

    @Transactional
    public ClientOrderResponse create(OrderRequest orderRequest){
        List<ItemValidatedResponse> validatedItems = coreService.validateOrderItems(orderRequest.cafeteriaId(), orderRequest.items());
        Order order = new Order();
        order.setCafeteriaId(orderRequest.cafeteriaId());
        order.setCafeteriaName(orderRequest.cafeteriaName());
        order.setClientName(orderRequest.clientName());
        order.setPaymentMethod(orderRequest.paymentMethod());
        order = orderRepository.save(order);

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        for(ItemValidatedResponse item: validatedItems){
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .itemId(item.id())
                    .itemName(item.name())
                    .quantity(item.quantity())
                    .uniquePrice(item.uniquePrice())
                    .build();
            orderItems.add(orderItem);
            totalAmount = totalAmount.add(orderItem.getUniquePrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())));
        }

        order.setTotalPrice(totalAmount);
        order.setItems(orderItems);
        order.setStatus(Status.PENDING);
        order = orderRepository.save(order);

        eventPublisher.publishEvent(new OrderCreatedEvent(order.getId(), order.getPaymentMethod()));
        return toClientOrderResponse(order);
    }

    private ClientOrderResponse toClientOrderResponse(Order order){
        List<OrderItemResponse> itemResponses = new ArrayList<>();
        for(OrderItem item: order.getItems()){
            OrderItemResponse orderItemResponse = new OrderItemResponse(
                    item.getItemName(),
                    item.getQuantity(),
                    item.getUniquePrice(),
                    item.getUniquePrice().multiply(BigDecimal.valueOf(item.getQuantity()))
            );
            itemResponses.add(orderItemResponse);
        }
        return new ClientOrderResponse(
                order.getSessionToken(),
                order.getCafeteriaName(),
                itemResponses,
                order.getTotalPrice(),
                order.getPaymentMethod(),
                order.getStatus(),
                order.getCreatedAt()
        );
    }
}
