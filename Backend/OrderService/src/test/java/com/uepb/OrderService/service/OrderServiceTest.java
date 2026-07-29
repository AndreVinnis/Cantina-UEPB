package com.uepb.OrderService.service;

import com.uepb.OrderService.domain.Order;
import com.uepb.OrderService.dto.events.OrderCreatedEvent;
import com.uepb.OrderService.dto.request.OrderItemRequest;
import com.uepb.OrderService.dto.request.OrderRequest;
import com.uepb.OrderService.dto.response.ClientOrderResponse;
import com.uepb.OrderService.enums.PaymentMethod;
import com.uepb.OrderService.enums.Status;
import com.uepb.OrderService.integration.CoreService;
import com.uepb.OrderService.integration.ItemValidatedResponse;
import com.uepb.OrderService.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CoreService coreService;

    @InjectMocks
    private OrderService orderService;

    private OrderRequest orderRequest;

    @BeforeEach
    void setup(){
        List<OrderItemRequest> itemRequests = List.of(
                new OrderItemRequest("Café Expresso", 2),
                new OrderItemRequest("Pão de Queijo", 1)
        );
        orderRequest = new OrderRequest(
                "Andre Vinicius",
                "qualquerId",
                "Cafeteria Central",
                itemRequests,
                PaymentMethod.PIX
        );
    }

    @Test
    @DisplayName("Deve criar um pedido com sucesso e calcular o valor total corretamente")
    void shouldCreateOrderSuccessfully() {
        ItemValidatedResponse item1 = new ItemValidatedResponse("1", "Café Expresso", 2, new BigDecimal("5.00"));
        ItemValidatedResponse item2 = new ItemValidatedResponse("2", "Pão de Queijo", 1, new BigDecimal("7.50"));
        List<ItemValidatedResponse> validatedItems = List.of(item1, item2);

        when(coreService.validateOrderItems(eq(orderRequest.cafeteriaId()), any())).thenReturn(validatedItems);

        when(orderRepository.save(any(Order.class))).then(AdditionalAnswers.returnsFirstArg());

        // Act
        ClientOrderResponse response = orderService.create(orderRequest);

        // Assert
        assertNotNull(response);
        assertEquals("Cafeteria Central", response.cafeteriaName());
        assertEquals(PaymentMethod.PIX, response.paymentMethod());
        assertEquals(Status.PENDING, response.status());
        assertEquals(new BigDecimal("17.50"), response.totalPrice());
        assertEquals(2, response.items().size());
        verify(orderRepository, times(2)).save(any(Order.class));
        verify(eventPublisher, times(1)).publishEvent(any(OrderCreatedEvent.class));
    }

    @Test
    @DisplayName("Não deve criar pedido se a validação dos itens falhar")
    void shouldThrowExceptionWhenValidationFails() {
        // Arrange
        when(coreService.validateOrderItems(eq(orderRequest.cafeteriaId()), any()))
                .thenThrow(new RuntimeException("Item indisponível ou inválido"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> orderService.create(orderRequest));

        assertEquals("Item indisponível ou inválido", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
        verify(eventPublisher, never()).publishEvent(any());
    }
}