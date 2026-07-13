package com.uepb.CoreService.config;

import com.uepb.CoreService.domain.Cafeteria;
import com.uepb.CoreService.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = JwtService.class)
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    @Mock
    private Cafeteria cafeteria;

    @Test
    void deveGerarTokenComSucesso() {
        when(cafeteria.getEmail()).thenReturn("contato@cafeteria.com");
        when(cafeteria.getId()).thenReturn("cafe-123");
        when(cafeteria.getRole()).thenReturn(UserRole.ADMIN);

        String token = jwtService.generateToken(cafeteria);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void deveExtrairEmailDoToken() {
        when(cafeteria.getEmail()).thenReturn("contato@cafeteria.com");
        when(cafeteria.getId()).thenReturn("cafe-123");
        when(cafeteria.getRole()).thenReturn(UserRole.USER);
        String token = jwtService.generateToken(cafeteria);

        String email = jwtService.extractEmail(token);

        assertEquals("contato@cafeteria.com", email);
    }

    @Test
    void deveExtrairUserIdDoToken() {
        when(cafeteria.getEmail()).thenReturn("contato@cafeteria.com");
        when(cafeteria.getId()).thenReturn("cafe-123");
        when(cafeteria.getRole()).thenReturn(UserRole.USER);
        String token = jwtService.generateToken(cafeteria);

        String userId = jwtService.extractUserId(token);

        assertEquals("cafe-123", userId);
    }

    @Test
    void deveExtrairRolesDoTokenCorretamente() {
        when(cafeteria.getEmail()).thenReturn("admin@cafeteria.com");
        when(cafeteria.getId()).thenReturn("cafe-999");
        when(cafeteria.getRole()).thenReturn(UserRole.ADMIN);

        String token = jwtService.generateToken(cafeteria);

        Set<UserRole> roles = jwtService.extractRoles(token);

        assertNotNull(roles);
        assertEquals(1, roles.size());
        assertTrue(roles.contains(UserRole.ADMIN));
    }

    @Test
    void deveRetornarTrueParaTokenValido() {
        when(cafeteria.getEmail()).thenReturn("valido@cafeteria.com");
        when(cafeteria.getId()).thenReturn("cafe-111");
        when(cafeteria.getRole()).thenReturn(UserRole.USER);
        String token = jwtService.generateToken(cafeteria);

        boolean isValid = jwtService.isValid(token);

        assertTrue(isValid);
    }

    @Test
    void deveRetornarFalseParaTokenAlteradoInvalido() {
        when(cafeteria.getEmail()).thenReturn("valido@cafeteria.com");
        when(cafeteria.getId()).thenReturn("cafe-111");
        when(cafeteria.getRole()).thenReturn(UserRole.USER);
        String token = jwtService.generateToken(cafeteria);

        String tokenAdulterado = token + "modificacao";

        boolean isValid = jwtService.isValid(tokenAdulterado);

        assertFalse(isValid);
    }

    @Test
    void deveRetornarFalseParaTokenExpirado() {
        long originalExpiration = (long) ReflectionTestUtils.getField(jwtService, "expirationMillis");

        try {
            ReflectionTestUtils.setField(jwtService, "expirationMillis", -10L);

            when(cafeteria.getEmail()).thenReturn("expirado@cafeteria.com");
            when(cafeteria.getId()).thenReturn("cafe-111");
            when(cafeteria.getRole()).thenReturn(UserRole.USER);

            String expiredToken = jwtService.generateToken(cafeteria);

            boolean isValid = jwtService.isValid(expiredToken);

            assertFalse(isValid);
        } finally {
            ReflectionTestUtils.setField(jwtService, "expirationMillis", originalExpiration);
        }
    }
}