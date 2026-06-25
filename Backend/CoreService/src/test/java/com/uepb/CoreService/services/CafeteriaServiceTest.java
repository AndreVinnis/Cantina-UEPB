package com.uepb.CoreService.services;

import com.uepb.CoreService.domain.Cafeteria;
import com.uepb.CoreService.dto.CafeteriaRequest;
import com.uepb.CoreService.enums.UserRole;
import com.uepb.CoreService.exceptions.EmailAlreadyExistException;
import com.uepb.CoreService.exceptions.ShortPasswordException;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class CafeteriaServiceTest {

    @Mock
    private CafeteriaRepository cafeteriaRepository;

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
        cafeteria.setImageUrl(null);

        request = new CafeteriaRequest("Central Perk",
                "contato@centralperk.com", "senhaForte123");

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
                .role(UserRole.USER)
                .build();

        when(cafeteriaRepository.findByEmail(request.email())).thenReturn(null);
        when(encoder.encode(request.password())).thenReturn("hashedPassword");
        when(cafeteriaRepository.save(any(Cafeteria.class))).thenReturn(savedCafeteria);

        // Act
        Cafeteria result = cafeteriaService.createCafeteria(request);

        // Assert
        assertNotNull(result);
        assertEquals("contato@centralperk.com", result.getEmail());

        // Verifica se os métodos foram chamados corretamente
        verify(cafeteriaRepository, times(1)).findByEmail(request.email());
        verify(encoder, times(1)).encode(request.password());

        // Captura o objeto salvo para verificar se foi montado corretamente antes de ir para o banco
        verify(cafeteriaRepository, times(1)).save(cafeteriaCaptor.capture());
        Cafeteria capturedCafeteria = cafeteriaCaptor.getValue();

        assertEquals("Central Perk", capturedCafeteria.getName());
        assertEquals("contato@centralperk.com", capturedCafeteria.getEmail());
        assertEquals("hashedPassword", capturedCafeteria.getHashPassword());
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
        verify(encoder, never()).encode(anyString());
        verify(cafeteriaRepository, never()).save(any(Cafeteria.class));
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando o formato do e-mail for inválido")
    void createCafeteria_ThrowsIllegalArgumentException_ForInvalidEmail() {
        // Arrange
        CafeteriaRequest request = new CafeteriaRequest("Central Perk", "email-invalido", "senhaForte123");

        when(cafeteriaRepository.findByEmail(request.email())).thenReturn(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> cafeteriaService.createCafeteria(request));

        assertEquals("Formato de e-mail inválido.", exception.getMessage());
        verify(encoder, never()).encode(anyString());
        verify(cafeteriaRepository, never()).save(any(Cafeteria.class));
    }

    @Test
    @DisplayName("Deve lançar ShortPasswordException quando a senha tiver menos de 8 caracteres")
    void createCafeteria_ThrowsShortPasswordException() {
        // Arrange
        CafeteriaRequest request = new CafeteriaRequest("Central Perk", "contato@centralperk.com", "1234567");

        when(cafeteriaRepository.findByEmail(request.email())).thenReturn(null);

        // Act & Assert
        assertThrows(ShortPasswordException.class, () -> cafeteriaService.createCafeteria(request));

        verify(encoder, never()).encode(anyString());
        verify(cafeteriaRepository, never()).save(any(Cafeteria.class));
    }

    @Test
    @DisplayName("Deve salvar a imagem com sucesso, atualizar a entidade e retornar o caminho")
    void deveSalvarImagemComSucesso() {
        // Arrange (Configuração do cenário)
        String caminhoEsperado = "/imagens/cafeterias/Cantina Central-123.png";

        // Simula o comportamento do serviço de armazenamento
        when(imageService.saveImage(any(MultipartFile.class), eq("cafeterias/"), eq("123"), eq("Cantina Central")))
                .thenReturn(caminhoEsperado);

        // Simula o salvamento no banco retornando a própria cafeteria
        when(cafeteriaRepository.save(any(Cafeteria.class))).thenReturn(cafeteria);

        // Act (Execução da ação)
        String resultadoCaminho = cafeteriaService.saveImage(cafeteria, mockFile);

        // Assert (Verificações e Validações)
        assertNotNull(resultadoCaminho);
        assertEquals(caminhoEsperado, resultadoCaminho);
        assertEquals(caminhoEsperado, cafeteria.getImageUrl(), "O objeto cafeteria deveria estar com a URL atualizada");

        // Verifica se os mocks foram acionados exatamente 1 vez com os parâmetros corretos
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

        // Garantia arquitetural: Se o arquivo falhou, o banco NÃO pode ser atualizado com uma URL inválida
        assertNull(cafeteria.getImageUrl());
        verify(cafeteriaRepository, never()).save(any(Cafeteria.class));
    }
}