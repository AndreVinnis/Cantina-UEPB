package com.uepb.OrderService.service;

import com.uepb.OrderService.domain.Order;
import com.uepb.OrderService.domain.OrderItem;
import com.uepb.OrderService.dto.events.OrderCreatedEvent;
import com.uepb.OrderService.dto.request.OrderItemRequest;
import com.uepb.OrderService.dto.request.OrderRequest;
import com.uepb.OrderService.dto.response.CafeteriaOrderResponse;
import com.uepb.OrderService.dto.response.ClientOrderResponse;
import com.uepb.OrderService.enums.PaymentMethod;
import com.uepb.OrderService.enums.Status;
import com.uepb.OrderService.exception.NoOrdersYet;
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
import java.util.ArrayList;
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
    private final String CAFETERIA_ID = "cafeteria-id-123";

    @BeforeEach
    void setup() {
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
        // Arrange
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

    @Test
    @DisplayName("Deve buscar apenas os pedidos abertos da cafeteria logada")
    void shouldReturnOpenOrdersSuccessfully() {
        // Arrange
        when(coreService.getIdCafeteria()).thenReturn(CAFETERIA_ID);

        Order openOrder = createMockOrder("1", Status.PENDING);
        Order closedOrder = createMockOrder("2", Status.CANCELLED);

        when(orderRepository.findByCafeteriaId(CAFETERIA_ID)).thenReturn(List.of(openOrder, closedOrder));

        // Act
        List<CafeteriaOrderResponse> responses = orderService.getOpenOrders();

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(openOrder.getId(), responses.getFirst().id());
        assertEquals(Status.PENDING, responses.getFirst().status());
        verify(coreService, times(1)).getIdCafeteria();
        verify(orderRepository, times(1)).findByCafeteriaId(CAFETERIA_ID);
    }

    @Test
    @DisplayName("Deve lançar NoOrdersYet ao buscar pedidos abertos quando a lista estiver vazia")
    void shouldThrowNoOrdersYetWhenOpenOrdersIsEmpty() {
        // Arrange
        when(coreService.getIdCafeteria()).thenReturn(CAFETERIA_ID);
        when(orderRepository.findByCafeteriaId(CAFETERIA_ID)).thenReturn(new ArrayList<>());

        // Act & Assert
        assertThrows(NoOrdersYet.class, () -> orderService.getOpenOrders());
        verify(coreService, times(1)).getIdCafeteria();
        verify(orderRepository, times(1)).findByCafeteriaId(CAFETERIA_ID);
    }

    @Test
    @DisplayName("Deve retornar todos os pedidos independentemente do status")
    void shouldReturnAllOrdersSuccessfully() {
        // Arrange
        when(coreService.getIdCafeteria()).thenReturn(CAFETERIA_ID);

        Order openOrder = createMockOrder("1", Status.PENDING);
        Order closedOrder = createMockOrder("2", Status.CANCELLED);

        when(orderRepository.findByCafeteriaId(CAFETERIA_ID)).thenReturn(List.of(openOrder, closedOrder));

        // Act
        List<CafeteriaOrderResponse> responses = orderService.getAllOrders();

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());
        verify(coreService, times(1)).getIdCafeteria();
        verify(orderRepository, times(1)).findByCafeteriaId(CAFETERIA_ID);
    }

    @Test
    @DisplayName("Deve lançar NoOrdersYet ao buscar todos os pedidos quando a lista estiver vazia")
    void shouldThrowNoOrdersYetWhenAllOrdersIsEmpty() {
        // Arrange
        when(coreService.getIdCafeteria()).thenReturn(CAFETERIA_ID);
        when(orderRepository.findByCafeteriaId(CAFETERIA_ID)).thenReturn(new ArrayList<>());

        // Act & Assert
        assertThrows(NoOrdersYet.class, () -> orderService.getAllOrders());
        verify(coreService, times(1)).getIdCafeteria();
        verify(orderRepository, times(1)).findByCafeteriaId(CAFETERIA_ID);
    }

    // Método utilitário para criar pedidos falsos populados para testes de mapeamento
    private Order createMockOrder(String id, Status status) {
        Order order = new Order();
        order.setId(id);
        order.setClientName("Cliente Teste");
        order.setCafeteriaId(CAFETERIA_ID);
        order.setCafeteriaName("Cafeteria Central");
        order.setPaymentMethod(PaymentMethod.PIX);
        order.setStatus(status);

        OrderItem orderItem = OrderItem.builder()
                .order(order)
                .itemId("item-1")
                .itemName("Café")
                .quantity(2)
                .uniquePrice(new BigDecimal("5.00"))
                .build();

        order.setItems(List.of(orderItem));
        order.setTotalPrice(new BigDecimal("10.00"));

        return order;
    }
}