package com.uepb.CoreService.services;

import com.uepb.CoreService.domain.Cafeteria;
import com.uepb.CoreService.domain.MenuItem;
import com.uepb.CoreService.dto.request.MenuItemRequest;
import com.uepb.CoreService.dto.response.MenuItemResponse;
import com.uepb.CoreService.enums.AvailabilityMode;
import com.uepb.CoreService.enums.Category;
import com.uepb.CoreService.exceptions.CafeteriaIsNotActive;
import com.uepb.CoreService.exceptions.MenuItemAlreadyExists;
import com.uepb.CoreService.exceptions.MenuItemNotFound;
import com.uepb.CoreService.exceptions.NoMenuItemsYet;
import com.uepb.CoreService.repository.MenuItemRepository;
import com.uepb.CoreService.utils.StorageImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class MenuItemServiceTest {

    @Mock
    private MenuItemRepository menuItemsRepository;

    @Mock
    private StorageImageService imageService;

    @InjectMocks
    private MenuItemService menuItemService;

    @Captor
    private ArgumentCaptor<MenuItem> menuItemCaptor;

    private Cafeteria activeCafeteria;
    private Cafeteria inactiveCafeteria;
    private MenuItemRequest request;
    private MenuItem menuItem;
    private MockMultipartFile mockFile;

    @BeforeEach
    void setUp() {
        // Configurando Cafeteria Ativa
        activeCafeteria = new Cafeteria();
        activeCafeteria.setId("cafe-123");
        activeCafeteria.setName("Cantina Central");
        activeCafeteria.setActive(true);

        // Configurando Cafeteria Inativa
        inactiveCafeteria = new Cafeteria();
        inactiveCafeteria.setId("cafe-456");
        inactiveCafeteria.setName("Cantina Fechada");
        inactiveCafeteria.setActive(false);

        // Assumindo tipos comuns para o Record/DTO baseado na entidade
        request = new MenuItemRequest(
                "Coxinha",
                "Coxinha de frango com catupiry",
                new BigDecimal("5.50"),
                Category.SNACK,
                AvailabilityMode.INVENTORY_CONTROL,
                20
        );

        // Entidade Mockada
        menuItem = MenuItem.builder()
                .id("item-123")
                .cafeteria(activeCafeteria)
                .name("Coxinha")
                .description("Coxinha de frango com catupiry")
                .price(new BigDecimal("5.50"))
                .category(Category.SNACK)
                .availabilityMode(AvailabilityMode.INVENTORY_CONTROL)
                .stock(20)
                .availability(true)
                .createdAt(Instant.now())
                .build();

        mockFile = new MockMultipartFile(
                "file",
                "coxinha.png",
                "image/png",
                "conteudo da imagem".getBytes()
        );
    }

    // --- TESTES PARA createMenuItem ---

    @Test
    @DisplayName("Deve criar um item no menu com sucesso quando a cafeteria estiver ativa e dados válidos")
    void createMenuItem_Success() {
        when(menuItemsRepository.findByCafeteriaId(activeCafeteria.getId())).thenReturn(Collections.emptyList());
        when(menuItemsRepository.save(any(MenuItem.class))).thenReturn(menuItem);

        MenuItemResponse result = menuItemService.createMenuItem(activeCafeteria, request);

        assertNotNull(result);
        assertEquals("Coxinha", result.name());
        assertEquals(new BigDecimal("5.50"), result.price());

        verify(menuItemsRepository, times(1)).findByCafeteriaId(activeCafeteria.getId());
        verify(menuItemsRepository, times(1)).save(menuItemCaptor.capture());

        MenuItem capturedItem = menuItemCaptor.getValue();
        assertEquals("Coxinha", capturedItem.getName());
        assertEquals(Category.SNACK, capturedItem.getCategory());
        assertEquals(20, capturedItem.getStock());
        assertEquals(activeCafeteria, capturedItem.getCafeteria());
    }

    @Test
    @DisplayName("Deve lançar CafeteriaIsNotActive quando tentar criar item em cafeteria inativa")
    void createMenuItem_ThrowsCafeteriaIsNotActive() {
        assertThrows(CafeteriaIsNotActive.class, () -> menuItemService.createMenuItem(inactiveCafeteria, request));

        verify(menuItemsRepository, never()).findByCafeteriaId(anyString());
        verify(menuItemsRepository, never()).save(any(MenuItem.class));
    }

    @Test
    @DisplayName("Deve lançar MenuItemAlreadyExists quando tentar criar item com nome já existente na mesma cafeteria")
    void createMenuItem_ThrowsMenuItemAlreadyExists() {
        MenuItem existingItem = MenuItem.builder().name("Coxinha").build();
        when(menuItemsRepository.findByCafeteriaId(activeCafeteria.getId())).thenReturn(List.of(existingItem));

        assertThrows(MenuItemAlreadyExists.class, () -> menuItemService.createMenuItem(activeCafeteria, request));

        verify(menuItemsRepository, never()).save(any(MenuItem.class));
    }

    // --- TESTES PARA saveImage ---

    @Test
    @DisplayName("Deve salvar a imagem com sucesso, atualizar a entidade do item e retornar o caminho")
    void saveImage_Success() {
        String subfolderEsperada = "items-Cantina Central/";
        String caminhoEsperado = "/imagens/items/Coxinha-item-123.png";

        when(menuItemsRepository.findByCafeteriaIdAndName(activeCafeteria.getId(), "Coxinha"))
                .thenReturn(Optional.of(menuItem));

        when(imageService.saveImage(any(MultipartFile.class), eq(subfolderEsperada), eq("item-123"), eq("Coxinha")))
                .thenReturn(caminhoEsperado);

        String resultadoCaminho = menuItemService.saveImage(activeCafeteria, "Coxinha", mockFile);

        assertNotNull(resultadoCaminho);
        assertEquals(caminhoEsperado, resultadoCaminho);
        assertEquals(caminhoEsperado, menuItem.getImageUrl(), "O objeto MenuItem deveria estar com a URL da imagem atualizada");

        verify(menuItemsRepository, times(1)).findByCafeteriaIdAndName(activeCafeteria.getId(), "Coxinha");
        verify(imageService, times(1)).saveImage(mockFile, subfolderEsperada, "item-123", "Coxinha");
        verify(menuItemsRepository, times(1)).save(menuItem);
    }

    @Test
    @DisplayName("Deve lançar MenuItemNotFound se o item não for encontrado na cafeteria ao tentar salvar imagem")
    void saveImage_ThrowsMenuItemNotFound() {
        when(menuItemsRepository.findByCafeteriaIdAndName(activeCafeteria.getId(), "Produto Inexistente"))
                .thenReturn(Optional.empty());

        assertThrows(MenuItemNotFound.class, () -> {
            menuItemService.saveImage(activeCafeteria, "Produto Inexistente", mockFile);
        });

        verify(imageService, never()).saveImage(any(), anyString(), anyString(), anyString());
        verify(menuItemsRepository, never()).save(any(MenuItem.class));
    }

    @Test
    @DisplayName("Deve propagar exceção e não salvar no banco se o StorageImageService falhar ao salvar imagem")
    void saveImage_ThrowsExceptionWhenStorageFails() {
        String subfolderEsperada = "items-Cantina Central/";

        when(menuItemsRepository.findByCafeteriaIdAndName(activeCafeteria.getId(), "Coxinha"))
                .thenReturn(Optional.of(menuItem));

        when(imageService.saveImage(any(), eq(subfolderEsperada), eq("item-123"), eq("Coxinha")))
                .thenThrow(new RuntimeException("Erro ao fazer upload da imagem no bucket"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            menuItemService.saveImage(activeCafeteria, "Coxinha", mockFile);
        });

        assertEquals("Erro ao fazer upload da imagem no bucket", exception.getMessage());

        assertNull(menuItem.getImageUrl());
        verify(menuItemsRepository, never()).save(any(MenuItem.class));
    }

    // --- TESTES PARA getMenuItemsForCafeteria ---

    @Test
    @DisplayName("Deve retornar apenas os itens que estão disponíveis da cafeteria")
    void getMenuItemsForCafeteria_Success() {
        MenuItem availableItem = MenuItem.builder()
                .id("item-1")
                .cafeteria(activeCafeteria)
                .name("Coxinha")
                .availability(true)
                .build();

        MenuItem unavailableItem = MenuItem.builder()
                .id("item-2")
                .cafeteria(activeCafeteria)
                .name("Bolo")
                .availability(false)
                .build();

        when(menuItemsRepository.findByCafeteriaId(activeCafeteria.getId()))
                .thenReturn(List.of(availableItem, unavailableItem));

        List<MenuItemResponse> result = menuItemService.getMenuItemsForCafeteria(activeCafeteria.getId());

        assertNotNull(result);
        assertEquals(1, result.size(), "Deve retornar apenas 1 item (o disponível)");
        assertEquals("Coxinha", result.get(0).name());
        verify(menuItemsRepository, times(1)).findByCafeteriaId(activeCafeteria.getId());
    }

    @Test
    @DisplayName("Deve lançar NoMenuItemsYet quando a cafeteria não possuir nenhum item cadastrado")
    void getMenuItemsForCafeteria_ThrowsNoMenuItemsYet() {
        when(menuItemsRepository.findByCafeteriaId(activeCafeteria.getId())).thenReturn(Collections.emptyList());

        assertThrows(NoMenuItemsYet.class, () -> menuItemService.getMenuItemsForCafeteria(activeCafeteria.getId()));
        verify(menuItemsRepository, times(1)).findByCafeteriaId(activeCafeteria.getId());
    }

    // --- TESTES PARA getAllMyItems ---

    @Test
    @DisplayName("Deve retornar todos os itens da cafeteria independente de estarem disponíveis")
    void getAllMyItems_Success() {
        MenuItem unavailableItem = MenuItem.builder()
                .id("item-2")
                .cafeteria(activeCafeteria)
                .name("Bolo")
                .availability(false)
                .build();

        when(menuItemsRepository.findByCafeteriaId(activeCafeteria.getId()))
                .thenReturn(List.of(menuItem, unavailableItem));

        List<MenuItemResponse> result = menuItemService.getAllMyItems(activeCafeteria);

        assertNotNull(result);
        assertEquals(2, result.size(), "Deve retornar todos os itens");
        verify(menuItemsRepository, times(1)).findByCafeteriaId(activeCafeteria.getId());
    }

    @Test
    @DisplayName("Deve lançar NoMenuItemsYet quando getAllMyItems encontrar lista vazia")
    void getAllMyItems_ThrowsNoMenuItemsYet() {
        when(menuItemsRepository.findByCafeteriaId(activeCafeteria.getId())).thenReturn(Collections.emptyList());

        assertThrows(NoMenuItemsYet.class, () -> menuItemService.getAllMyItems(activeCafeteria));
    }

    // --- TESTES PARA addStock ---

    @Test
    @DisplayName("Deve adicionar estoque a um item existente com sucesso")
    void addStock_Success() {
        when(menuItemsRepository.findByCafeteriaIdAndName(activeCafeteria.getId(), "Coxinha"))
                .thenReturn(Optional.of(menuItem));
        when(menuItemsRepository.save(any(MenuItem.class))).thenAnswer(i -> i.getArgument(0));

        MenuItemResponse result = menuItemService.addStock(activeCafeteria, "Coxinha", 10);

        assertNotNull(result);
        assertEquals(30, menuItem.getStock());
        assertEquals(30, result.stock());
        verify(menuItemsRepository, times(1)).save(menuItem);
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando tentar adicionar quantidade menor ou igual a zero")
    void addStock_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> menuItemService.addStock(activeCafeteria, "Coxinha", 0));
        assertThrows(IllegalArgumentException.class, () -> menuItemService.addStock(activeCafeteria, "Coxinha", -5));

        verify(menuItemsRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar MenuItemNotFound ao tentar adicionar estoque a item inexistente")
    void addStock_ThrowsMenuItemNotFound() {
        when(menuItemsRepository.findByCafeteriaIdAndName(activeCafeteria.getId(), "Fantasma"))
                .thenReturn(Optional.empty());

        assertThrows(MenuItemNotFound.class, () -> menuItemService.addStock(activeCafeteria, "Fantasma", 10));
    }

    // --- TESTES PARA removeStock ---

    @Test
    @DisplayName("Deve remover estoque de um item com sucesso quando houver saldo suficiente")
    void removeStock_Success() {
        when(menuItemsRepository.findByCafeteriaIdAndName(activeCafeteria.getId(), "Coxinha"))
                .thenReturn(Optional.of(menuItem));
        when(menuItemsRepository.save(any(MenuItem.class))).thenAnswer(i -> i.getArgument(0));

        MenuItemResponse result = menuItemService.removeStock(activeCafeteria, "Coxinha", 5);

        assertNotNull(result);
        assertEquals(15, menuItem.getStock());
        assertEquals(15, result.stock());
        verify(menuItemsRepository, times(1)).save(menuItem);
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando quantidade a remover for menor ou igual a zero")
    void removeStock_ThrowsIllegalArgumentException_InvalidQuantity() {
        assertThrows(IllegalArgumentException.class, () -> menuItemService.removeStock(activeCafeteria, "Coxinha", 0));
        assertThrows(IllegalArgumentException.class, () -> menuItemService.removeStock(activeCafeteria, "Coxinha", -10));

        verify(menuItemsRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando tentar remover quantidade superior ao estoque atual")
    void removeStock_ThrowsIllegalArgumentException_InsufficientStock() {
        when(menuItemsRepository.findByCafeteriaIdAndName(activeCafeteria.getId(), "Coxinha"))
                .thenReturn(Optional.of(menuItem));

        assertThrows(IllegalArgumentException.class, () -> menuItemService.removeStock(activeCafeteria, "Coxinha", 50));
        verify(menuItemsRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar MenuItemNotFound ao tentar remover estoque de item inexistente")
    void removeStock_ThrowsMenuItemNotFound() {
        when(menuItemsRepository.findByCafeteriaIdAndName(activeCafeteria.getId(), "Fantasma"))
                .thenReturn(Optional.empty());

        assertThrows(MenuItemNotFound.class, () -> menuItemService.removeStock(activeCafeteria, "Fantasma", 5));
    }

    // --- TESTES PARA changeAvailability ---

    @Test
    @DisplayName("Deve alterar a disponibilidade de um item com sucesso")
    void changeAvailability_Success() {
        when(menuItemsRepository.findByCafeteriaIdAndName(activeCafeteria.getId(), "Coxinha"))
                .thenReturn(Optional.of(menuItem));
        when(menuItemsRepository.save(any(MenuItem.class))).thenAnswer(i -> i.getArgument(0));

        // Inicialmente true no setUp()
        menuItemService.changeAvailability(activeCafeteria, "Coxinha", false);

        assertFalse(menuItem.isAvailability());
        verify(menuItemsRepository, times(1)).save(menuItem);
    }

    @Test
    @DisplayName("Deve lançar MenuItemNotFound ao tentar alterar disponibilidade de item inexistente")
    void changeAvailability_ThrowsMenuItemNotFound() {
        when(menuItemsRepository.findByCafeteriaIdAndName(activeCafeteria.getId(), "Fantasma"))
                .thenReturn(Optional.empty());

        assertThrows(MenuItemNotFound.class, () -> menuItemService.changeAvailability(activeCafeteria, "Fantasma", false));
    }

    // --- TESTES PARA updateItem ---

    @Test
    @DisplayName("Deve atualizar campos não nulos de um item com sucesso")
    void updateItem_Success() {
        when(menuItemsRepository.findByCafeteriaIdAndName(activeCafeteria.getId(), "Coxinha"))
                .thenReturn(Optional.of(menuItem));
        when(menuItemsRepository.save(any(MenuItem.class))).thenAnswer(i -> i.getArgument(0));

        MenuItemRequest updateRequest = new MenuItemRequest(
                "Coxinha Especial",
                "Coxinha de frango com requeijão",
                new BigDecimal("6.00"),
                Category.SNACK,
                AvailabilityMode.INVENTORY_CONTROL,
                null // Stock nulo, não deve atualizar o estoque existente (20)
        );

        MenuItemResponse result = menuItemService.updateItem(activeCafeteria, "Coxinha", updateRequest);

        assertNotNull(result);
        assertEquals("Coxinha Especial", menuItem.getName());
        assertEquals("Coxinha de frango com requeijão", menuItem.getDescription());
        assertEquals(new BigDecimal("6.00"), menuItem.getPrice());
        assertEquals(20, menuItem.getStock(), "O estoque não deve ter sido modificado pois o valor no updateRequest era null");
        verify(menuItemsRepository, times(1)).save(menuItem);
    }

    @Test
    @DisplayName("Deve lançar MenuItemNotFound ao tentar atualizar item inexistente")
    void updateItem_ThrowsMenuItemNotFound() {
        when(menuItemsRepository.findByCafeteriaIdAndName(activeCafeteria.getId(), "Fantasma"))
                .thenReturn(Optional.empty());

        assertThrows(MenuItemNotFound.class, () -> menuItemService.updateItem(activeCafeteria, "Fantasma", request));
    }

    // --- TESTES PARA deleteItem ---

    @Test
    @DisplayName("Deve deletar um item existente com sucesso")
    void deleteItem_Success() {
        when(menuItemsRepository.findByCafeteriaIdAndName(activeCafeteria.getId(), "Coxinha"))
                .thenReturn(Optional.of(menuItem));

        menuItemService.deleteItem(activeCafeteria, "Coxinha");

        verify(menuItemsRepository, times(1)).delete(menuItem);
    }

    @Test
    @DisplayName("Deve lançar MenuItemNotFound ao tentar deletar item inexistente")
    void deleteItem_ThrowsMenuItemNotFound() {
        when(menuItemsRepository.findByCafeteriaIdAndName(activeCafeteria.getId(), "Fantasma"))
                .thenReturn(Optional.empty());

        assertThrows(MenuItemNotFound.class, () -> menuItemService.deleteItem(activeCafeteria, "Fantasma"));
        verify(menuItemsRepository, never()).delete(any());
    }
}