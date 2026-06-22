package com.uepb.CoreService.services;

import com.uepb.CoreService.domain.Cafeteria;
import com.uepb.CoreService.dto.CafeteriaRequest;
import com.uepb.CoreService.enums.UserRole;
import com.uepb.CoreService.exceptions.EmailAlreadyExistException;
import com.uepb.CoreService.exceptions.ShortPasswordException;
import com.uepb.CoreService.repository.CafeteriaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CafeteriaServiceTest {

    @Mock
    private CafeteriaRepository cafeteriaRepository;

    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private CafeteriaService cafeteriaService;

    @Captor
    private ArgumentCaptor<Cafeteria> cafeteriaCaptor;

    @Test
    @DisplayName("Deve criar uma cafeteria com sucesso quando os dados forem válidos")
    void createCafeteria_Success() {
        // Arrange
        CafeteriaRequest request = new CafeteriaRequest("Central Perk", "contato@centralperk.com", "senhaForte123");
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
        CafeteriaRequest request = new CafeteriaRequest("Central Perk", "contato@centralperk.com", "senhaForte123");
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
        CafeteriaRequest request = new CafeteriaRequest("Central Perk", "contato@centralperk.com", "1234567"); // 7 caracteres

        when(cafeteriaRepository.findByEmail(request.email())).thenReturn(null);

        // Act & Assert
        assertThrows(ShortPasswordException.class, () -> cafeteriaService.createCafeteria(request));

        verify(encoder, never()).encode(anyString());
        verify(cafeteriaRepository, never()).save(any(Cafeteria.class));
    }
}