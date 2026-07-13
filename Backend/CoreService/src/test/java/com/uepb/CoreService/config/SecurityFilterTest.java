package com.uepb.CoreService.config;

import com.uepb.CoreService.services.AuthorizationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityFilterTest {

    @InjectMocks
    private SecurityFilter securityFilter;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthorizationService authorizationService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private UserDetails userDetails;

    @Mock
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveProsseguirSemAutenticarQuandoNaoHouverHeaderAuthorization() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        securityFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtService, authorizationService);
    }

    @Test
    void deveProsseguirSemAutenticarQuandoHeaderNaoComecarComBearer() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

        // Act
        securityFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtService, authorizationService);
    }

    @Test
    void deveProsseguirSemAutenticarQuandoTokenForInvalido() throws ServletException, IOException {
        // Arrange
        String token = "token.invalido.123";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.isValid(token)).thenReturn(false);

        // Act
        securityFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtService, never()).extractEmail(anyString());
        verifyNoInteractions(authorizationService);
    }

    @Test
    void deveAutenticarUsuarioComSucessoQuandoTokenForValido() throws ServletException, IOException {
        // Arrange
        String token = "token.valido.123";
        String email = "teste@cafeteria.com";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.isValid(token)).thenReturn(true);
        when(jwtService.extractEmail(token)).thenReturn(email);

        // Simula o retorno das authorities (roles) vazias apenas para não dar NullPointerException
        when(userDetails.getAuthorities()).thenReturn(Collections.emptyList());
        when(authorizationService.loadUserByUsername(email)).thenReturn(userDetails);

        // Act
        securityFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(userDetails, authentication.getPrincipal());
    }

    @Test
    void deveRetornarErro401QuandoUsuarioNaoForEncontrado() throws ServletException, IOException {
        // Arrange
        String token = "token.valido.mas.usuario.deletado";
        String email = "deletado@cafeteria.com";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.isValid(token)).thenReturn(true);
        when(jwtService.extractEmail(token)).thenReturn(email);

        when(authorizationService.loadUserByUsername(email))
                .thenThrow(new UsernameNotFoundException("Usuário não encontrado"));

        when(response.getWriter()).thenReturn(printWriter);

        // Act
        securityFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, never()).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");
        verify(printWriter).write("{\"message\":\"Sessao invalida ou expirada\"}");
    }
}