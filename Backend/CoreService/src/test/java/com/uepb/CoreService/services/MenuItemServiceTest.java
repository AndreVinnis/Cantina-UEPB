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
        // Arrange
        when(menuItemsRepository.findByCafeteriaId(activeCafeteria.getId())).thenReturn(Collections.emptyList());
        when(menuItemsRepository.save(any(MenuItem.class))).thenReturn(menuItem);

        // Act
        MenuItemResponse result = menuItemService.createMenuItem(activeCafeteria, request);

        // Assert
        assertNotNull(result);
        assertEquals("Coxinha", result.name());
        assertEquals(new BigDecimal("5.50"), result.price());

        // Verifica se os métodos foram chamados corretamente
        verify(menuItemsRepository, times(1)).findByCafeteriaId(activeCafeteria.getId());
        verify(menuItemsRepository, times(1)).save(menuItemCaptor.capture());

        // Verifica a montagem do objeto salvo
        MenuItem capturedItem = menuItemCaptor.getValue();
        assertEquals("Coxinha", capturedItem.getName());
        assertEquals(Category.SNACK, capturedItem.getCategory());
        assertEquals(20, capturedItem.getStock());
        assertEquals(activeCafeteria, capturedItem.getCafeteria());
    }

    @Test
    @DisplayName("Deve lançar CafeteriaIsNotActive quando tentar criar item em cafeteria inativa")
    void createMenuItem_ThrowsCafeteriaIsNotActive() {
        // Act & Assert
        assertThrows(CafeteriaIsNotActive.class, () -> menuItemService.createMenuItem(inactiveCafeteria, request));

        // Verifica que o banco de dados não foi tocado
        verify(menuItemsRepository, never()).findByCafeteriaId(anyString());
        verify(menuItemsRepository, never()).save(any(MenuItem.class));
    }

    @Test
    @DisplayName("Deve lançar MenuItemAlreadyExists quando tentar criar item com nome já existente na mesma cafeteria")
    void createMenuItem_ThrowsMenuItemAlreadyExists() {
        // Arrange
        MenuItem existingItem = MenuItem.builder().name("Coxinha").build();
        when(menuItemsRepository.findByCafeteriaId(activeCafeteria.getId())).thenReturn(List.of(existingItem));

        // Act & Assert
        assertThrows(MenuItemAlreadyExists.class, () -> menuItemService.createMenuItem(activeCafeteria, request));

        // Verifica que não chegou a salvar no banco
        verify(menuItemsRepository, never()).save(any(MenuItem.class));
    }

    // --- TESTES PARA saveImage ---

    @Test
    @DisplayName("Deve salvar a imagem com sucesso, atualizar a entidade do item e retornar o caminho")
    void saveImage_Success() {
        // Arrange
        String subfolderEsperada = "items-Cantina Central/";
        String caminhoEsperado = "/imagens/items/Coxinha-item-123.png";

        when(menuItemsRepository.findByCafeteriaIdAndName(activeCafeteria.getId(), "Coxinha"))
                .thenReturn(Optional.of(menuItem));

        when(imageService.saveImage(any(MultipartFile.class), eq(subfolderEsperada), eq("item-123"), eq("Coxinha")))
                .thenReturn(caminhoEsperado);

        // Act
        String resultadoCaminho = menuItemService.saveImage(activeCafeteria, "Coxinha", mockFile);

        // Assert
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
        // Arrange
        when(menuItemsRepository.findByCafeteriaIdAndName(activeCafeteria.getId(), "Produto Inexistente"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(MenuItemNotFound.class, () -> {
            menuItemService.saveImage(activeCafeteria, "Produto Inexistente", mockFile);
        });

        // Garantia de que não interagiu com Storage nem salvou
        verify(imageService, never()).saveImage(any(), anyString(), anyString(), anyString());
        verify(menuItemsRepository, never()).save(any(MenuItem.class));
    }

    @Test
    @DisplayName("Deve propagar exceção e não salvar no banco se o StorageImageService falhar ao salvar imagem do item")
    void saveImage_ThrowsExceptionWhenStorageFails() {
        // Arrange
        String subfolderEsperada = "items-Cantina Central/";

        when(menuItemsRepository.findByCafeteriaIdAndName(activeCafeteria.getId(), "Coxinha"))
                .thenReturn(Optional.of(menuItem));

        when(imageService.saveImage(any(), eq(subfolderEsperada), eq("item-123"), eq("Coxinha")))
                .thenThrow(new RuntimeException("Erro ao fazer upload da imagem no bucket"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            menuItemService.saveImage(activeCafeteria, "Coxinha", mockFile);
        });

        assertEquals("Erro ao fazer upload da imagem no bucket", exception.getMessage());

        // Garantia arquitetural de que o "item" não foi atualizado no banco com URL inválida
        assertNull(menuItem.getImageUrl());
        verify(menuItemsRepository, never()).save(any(MenuItem.class));
    }
}