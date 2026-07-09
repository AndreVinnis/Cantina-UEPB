package com.uepb.CoreService.services;

import com.uepb.CoreService.domain.Cafeteria;
import com.uepb.CoreService.dto.request.CafeteriaRequest;
import com.uepb.CoreService.dto.response.CafeteriaResponse;
import com.uepb.CoreService.dto.response.MenuItemResponse;
import com.uepb.CoreService.enums.Campus;
import com.uepb.CoreService.enums.UserRole;
import com.uepb.CoreService.exceptions.*;
import com.uepb.CoreService.repository.CafeteriaRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class CafeteriaServiceTest {

    @Mock
    private CafeteriaRepository cafeteriaRepository;

    @Mock
    private MenuItemService menuItemService;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private StorageImageService imageService;

    @InjectMocks
    private CafeteriaService cafeteriaService;

    @Captor
    private ArgumentCaptor<Cafeteria> cafeteriaCaptor;

    private Cafeteria cafeteria;
    private CafeteriaRequest request;
    private MockMultipartFile mockFile;

    @BeforeEach
    void setUp() {
        cafeteria = new Cafeteria();
        cafeteria.setId("123");
        cafeteria.setName("Cantina Central");
        cafeteria.setEmail("contato@centralperk.com");
        cafeteria.setHashPassword("oldHashedPassword");
        cafeteria.setCampus(Campus.CAMPUS_VII);
        cafeteria.setActive(true);
        cafeteria.setImageUrl(null);

        request = new CafeteriaRequest("Central Perk",
                "contato@centralperk.com", "senhaForte123", Campus.CAMPUS_VII);

        mockFile = new MockMultipartFile(
                "file",
                "logo.png",
                "image/png",
                "conteudo da imagem".getBytes()
        );
    }

    @Test
    @DisplayName("Deve criar uma cafeteria com sucesso quando os dados forem válidos")
    void createCafeteria_Success() {
        // Arrange
        Cafeteria savedCafeteria = Cafeteria.builder()
                .name(request.name())
                .email(request.email())
                .hashPassword("hashedPassword")
                .campus(request.campus())
                .role(UserRole.USER)
                .build();

        when(cafeteriaRepository.findByEmail(request.email())).thenReturn(null);
        when(cafeteriaRepository.findByCampus(request.campus())).thenReturn(Collections.emptyList());
        when(encoder.encode(request.password())).thenReturn("hashedPassword");
        when(cafeteriaRepository.save(any(Cafeteria.class))).thenReturn(savedCafeteria);

        // Act
        Cafeteria result = cafeteriaService.createCafeteria(request);

        // Assert
        assertNotNull(result);
        assertEquals("contato@centralperk.com", result.getEmail());

        // Verifica se os métodos foram chamados corretamente
        verify(cafeteriaRepository, times(1)).findByEmail(request.email());
        verify(cafeteriaRepository, times(1)).findByCampus(request.campus());
        verify(encoder, times(1)).encode(request.password());

        // Captura o objeto salvo para verificar se foi montado corretamente antes de ir para o banco
        verify(cafeteriaRepository, times(1)).save(cafeteriaCaptor.capture());
        Cafeteria capturedCafeteria = cafeteriaCaptor.getValue();

        assertEquals("Central Perk", capturedCafeteria.getName());
        assertEquals("contato@centralperk.com", capturedCafeteria.getEmail());
        assertEquals("hashedPassword", capturedCafeteria.getHashPassword());
        assertEquals(Campus.CAMPUS_VII, capturedCafeteria.getCampus());
        assertEquals(UserRole.USER, capturedCafeteria.getRole());
    }

    @Test
    @DisplayName("Deve lançar EmailAlreadyExistException quando o e-mail já estiver em uso")
    void createCafeteria_ThrowsEmailAlreadyExistException() {
        // Arrange
        Cafeteria existingCafeteria = Cafeteria.builder().email("contato@centralperk.com").build();
        when(cafeteriaRepository.findByEmail(request.email())).thenReturn(existingCafeteria);

        // Act & Assert
        assertThrows(EmailAlreadyExistException.class, () -> cafeteriaService.createCafeteria(request));

        // Verifica que não chegou a encodar a senha nem salvar no banco
        verify(cafeteriaRepository, never()).findByCampus(any(Campus.class));
        verify(encoder, never()).encode(anyString());
        verify(cafeteriaRepository, never()).save(any(Cafeteria.class));
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando o formato do e-mail for inválido")
    void createCafeteria_ThrowsIllegalArgumentException_ForInvalidEmail() {
        // Arrange
        CafeteriaRequest invalidEmailRequest = new CafeteriaRequest("Central Perk", "email-invalido", "senhaForte123", Campus.CAMPUS_VII);
        when(cafeteriaRepository.findByEmail(invalidEmailRequest.email())).thenReturn(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> cafeteriaService.createCafeteria(invalidEmailRequest));

        assertEquals("Formato de e-mail inválido.", exception.getMessage());
        verify(encoder, never()).encode(anyString());
        verify(cafeteriaRepository, never()).save(any(Cafeteria.class));
    }

    @Test
    @DisplayName("Deve lançar ShortPasswordException quando a senha tiver menos de 8 caracteres")
    void createCafeteria_ThrowsShortPasswordException() {
        // Arrange
        CafeteriaRequest shortPassRequest = new CafeteriaRequest("Central Perk", "contato@centralperk.com", "1234567", Campus.CAMPUS_VII);
        when(cafeteriaRepository.findByEmail(shortPassRequest.email())).thenReturn(null);

        // Act & Assert
        assertThrows(ShortPasswordException.class, () -> cafeteriaService.createCafeteria(shortPassRequest));

        verify(encoder, never()).encode(anyString());
        verify(cafeteriaRepository, never()).save(any(Cafeteria.class));
    }

    @Test
    @DisplayName("Deve lançar NameAlreadyExist quando o nome da cafeteria já existir no mesmo campus")
    void createCafeteria_ThrowsNameAlreadyExist() {
        // Arrange
        when(cafeteriaRepository.findByEmail(request.email())).thenReturn(null);

        Cafeteria existingCafeteria = new Cafeteria();
        existingCafeteria.setName(request.name());
        existingCafeteria.setCampus(request.campus());

        when(cafeteriaRepository.findByCampus(request.campus())).thenReturn(List.of(existingCafeteria));

        // Act & Assert
        assertThrows(NameAlreadyExist.class, () -> cafeteriaService.createCafeteria(request));

        verify(encoder, never()).encode(anyString());
        verify(cafeteriaRepository, never()).save(any(Cafeteria.class));
    }

    @Test
    @DisplayName("Deve retornar os dados da cafeteria logada com sucesso")
    void getMyCafeteria_Success() {
        // Arrange
        when(cafeteriaRepository.findByEmail(cafeteria.getEmail())).thenReturn(cafeteria);

        // Act
        CafeteriaResponse response = cafeteriaService.getMyCafeteria(cafeteria.getEmail());

        // Assert
        assertNotNull(response);
        assertEquals(cafeteria.getName(), response.name());
        assertEquals(cafeteria.getEmail(), response.email());
        assertEquals(cafeteria.isActive(), response.active());
        verify(cafeteriaRepository, times(1)).findByEmail(cafeteria.getEmail());
    }

    @Test
    @DisplayName("Deve lançar CafeteriaNotFound ao tentar buscar cafeteria que não existe")
    void getMyCafeteria_ThrowsCafeteriaNotFound() {
        // Arrange
        String emailInexistente = "naoexiste@email.com";
        when(cafeteriaRepository.findByEmail(emailInexistente)).thenReturn(null);

        // Act & Assert
        assertThrows(CafeteriaNotFound.class, () -> cafeteriaService.getMyCafeteria(emailInexistente));
        verify(cafeteriaRepository, times(1)).findByEmail(emailInexistente);
    }

    @Test
    @DisplayName("Deve atualizar os dados da cafeteria com sucesso")
    void updateCafeteria_Success() {
        // Arrange
        CafeteriaRequest updateRequest = new CafeteriaRequest("Novo Nome", "novo@email.com", "novaSenha123", Campus.CAMPUS_VII);
        when(cafeteriaRepository.findByEmail(cafeteria.getEmail())).thenReturn(cafeteria);
        when(encoder.encode(updateRequest.password())).thenReturn("newHashedPassword");
        when(cafeteriaRepository.save(any(Cafeteria.class))).thenReturn(cafeteria);

        // Act
        CafeteriaResponse response = cafeteriaService.updateCafeteria(cafeteria.getEmail(), updateRequest);

        // Assert
        assertNotNull(response);
        assertEquals("Novo Nome", response.name());
        assertEquals("novo@email.com", response.email());

        verify(cafeteriaRepository, times(1)).findByEmail("contato@centralperk.com");
        verify(encoder, times(1)).encode("novaSenha123");
        verify(cafeteriaRepository, times(1)).save(cafeteria);
        assertEquals("newHashedPassword", cafeteria.getHashPassword());
    }

    @Test
    @DisplayName("Deve permitir atualização parcial sem sobrescrever os outros dados com nulo")
    void updateCafeteria_PartialUpdate() {
        // Arrange (Request apenas com o nome para alterar)
        CafeteriaRequest partialRequest = new CafeteriaRequest("Apenas Novo Nome", null, null, null);
        when(cafeteriaRepository.findByEmail(cafeteria.getEmail())).thenReturn(cafeteria);
        when(cafeteriaRepository.save(any(Cafeteria.class))).thenReturn(cafeteria);

        // Act
        CafeteriaResponse response = cafeteriaService.updateCafeteria(cafeteria.getEmail(), partialRequest);

        // Assert
        assertEquals("Apenas Novo Nome", response.name());
        assertEquals("contato@centralperk.com", response.email()); // E-mail deve permanecer o antigo
        assertEquals("oldHashedPassword", cafeteria.getHashPassword()); // Senha não deve ser alterada

        verify(encoder, never()).encode(anyString()); // Não deve chamar o encoder se não mandou senha
        verify(cafeteriaRepository, times(1)).save(cafeteria);
    }

    @Test
    @DisplayName("Deve lançar CafeteriaNotFound ao tentar atualizar cafeteria que não existe")
    void updateCafeteria_ThrowsCafeteriaNotFound() {
        // Arrange
        String emailInexistente = "naoexiste@email.com";
        when(cafeteriaRepository.findByEmail(emailInexistente)).thenReturn(null);

        // Act & Assert
        assertThrows(CafeteriaNotFound.class, () -> cafeteriaService.updateCafeteria(emailInexistente, request));
        verify(cafeteriaRepository, never()).save(any(Cafeteria.class));
    }

    @Test
    @DisplayName("Deve deletar uma cafeteria com sucesso")
    void delete_Success() {
        // Arrange
        when(cafeteriaRepository.findByEmail(cafeteria.getEmail())).thenReturn(cafeteria);

        // Act
        cafeteriaService.delete(cafeteria.getEmail());

        // Assert
        verify(cafeteriaRepository, times(1)).findByEmail(cafeteria.getEmail());
        verify(cafeteriaRepository, times(1)).delete(cafeteria);
    }

    @Test
    @DisplayName("Deve lançar CafeteriaNotFound ao tentar deletar uma cafeteria inexistente")
    void delete_ThrowsCafeteriaNotFound() {
        // Arrange
        String emailInexistente = "naoexiste@email.com";
        when(cafeteriaRepository.findByEmail(emailInexistente)).thenReturn(null);

        // Act & Assert
        assertThrows(CafeteriaNotFound.class, () -> cafeteriaService.delete(emailInexistente));
        verify(cafeteriaRepository, never()).delete(any(Cafeteria.class));
    }

    @Test
    @DisplayName("Deve salvar a imagem com sucesso, atualizar a entidade e retornar o caminho")
    void deveSalvarImagemComSucesso() {
        // Arrange
        String caminhoEsperado = "/imagens/cafeterias/Cantina Central-123.png";

        when(imageService.saveImage(any(MultipartFile.class), eq("cafeterias/"), eq("123"), eq("Cantina Central")))
                .thenReturn(caminhoEsperado);
        when(cafeteriaRepository.save(any(Cafeteria.class))).thenReturn(cafeteria);

        // Act
        String resultadoCaminho = cafeteriaService.saveImage(cafeteria, mockFile);

        // Assert
        assertNotNull(resultadoCaminho);
        assertEquals(caminhoEsperado, resultadoCaminho);
        assertEquals(caminhoEsperado, cafeteria.getImageUrl(), "O objeto cafeteria deveria estar com a URL atualizada");

        verify(imageService, times(1)).saveImage(mockFile, "cafeterias/", "123", "Cantina Central");
        verify(cafeteriaRepository, times(1)).save(cafeteria);
    }

    @Test
    @DisplayName("Deve lançar exceção e não salvar no banco se o StorageImageService falhar")
    void deveLancatExcecaoQuandoStorageFalhar() {
        // Arrange
        when(imageService.saveImage(any(), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Erro ao salvar a imagem"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cafeteriaService.saveImage(cafeteria, mockFile);
        });

        assertEquals("Erro ao salvar a imagem", exception.getMessage());
        assertNull(cafeteria.getImageUrl());
        verify(cafeteriaRepository, never()).save(any(Cafeteria.class));
    }

    @Test
    @DisplayName("Deve retornar lista de cafeterias com sucesso ao buscar por campus")
    void getCafeteriaByCampus_Success() {
        // Arrange
        when(cafeteriaRepository.findByCampus(Campus.CAMPUS_VII)).thenReturn(List.of(cafeteria));

        // Act
        List<CafeteriaResponse> responseList = cafeteriaService.getCafeteriaByCampus(Campus.CAMPUS_VII);

        // Assert
        assertNotNull(responseList);
        assertFalse(responseList.isEmpty());
        assertEquals(1, responseList.size());
        assertEquals(cafeteria.getName(), responseList.get(0).name());
        verify(cafeteriaRepository, times(1)).findByCampus(Campus.CAMPUS_VII);
    }

    @Test
    @DisplayName("Deve lançar NoCafeteriaFound ao buscar cafeterias em campus sem registros")
    void getCafeteriaByCampus_ThrowsNoCafeteriaFound() {
        // Arrange
        when(cafeteriaRepository.findByCampus(Campus.CAMPUS_VII)).thenReturn(Collections.emptyList());

        // Act & Assert
        assertThrows(NoCafeteriaFound.class, () -> cafeteriaService.getCafeteriaByCampus(Campus.CAMPUS_VII));
        verify(cafeteriaRepository, times(1)).findByCampus(Campus.CAMPUS_VII);
    }

    @Test
    @DisplayName("Deve retornar os itens do menu da cafeteria corretamente")
    void getItemsForCafeteria_Success() {
        // Arrange
        when(cafeteriaRepository.findByNameAndCampus(cafeteria.getName(), Campus.CAMPUS_VII))
                .thenReturn(Optional.of(cafeteria));

        MenuItemResponse mockResponse = mock(MenuItemResponse.class);
        when(menuItemService.getMenuItemsForCafeteria(cafeteria.getId())).thenReturn(List.of(mockResponse));

        // Act
        List<MenuItemResponse> result = cafeteriaService.getItemsForCafeteria(cafeteria.getName(), Campus.CAMPUS_VII);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(cafeteriaRepository, times(1)).findByNameAndCampus(cafeteria.getName(), Campus.CAMPUS_VII);
        verify(menuItemService, times(1)).getMenuItemsForCafeteria(cafeteria.getId());
    }

    @Test
    @DisplayName("Deve lançar CafeteriaNotFound ao tentar buscar itens de cafeteria que não existe")
    void getItemsForCafeteria_ThrowsCafeteriaNotFound() {
        // Arrange
        String nomeInexistente = "Cantina Fantasma";
        when(cafeteriaRepository.findByNameAndCampus(nomeInexistente, Campus.CAMPUS_VII))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CafeteriaNotFound.class, () -> cafeteriaService.getItemsForCafeteria(nomeInexistente, Campus.CAMPUS_VII));

        verify(cafeteriaRepository, times(1)).findByNameAndCampus(nomeInexistente, Campus.CAMPUS_VII);
        verify(menuItemService, never()).getMenuItemsForCafeteria(anyString());
    }
}