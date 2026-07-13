package com.uepb.CoreService.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uepb.CoreService.config.JwtService;
import com.uepb.CoreService.domain.Cafeteria;
import com.uepb.CoreService.dto.request.CafeteriaRequest;
import com.uepb.CoreService.dto.response.CafeteriaResponse;
import com.uepb.CoreService.dto.response.MenuItemResponse;
import com.uepb.CoreService.enums.Campus;
import com.uepb.CoreService.enums.Category;
import com.uepb.CoreService.services.AuthorizationService;
import com.uepb.CoreService.services.CafeteriaService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CafeteriaController.class)
@AutoConfigureMockMvc(addFilters = false)
class CafeteriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CafeteriaService cafeteriaService;

    @MockBean
    private AuthorizationService authorizationService;

    @MockBean
    private JwtService jwtService;

    private Cafeteria mockCafeteria;
    private CafeteriaRequest request;
    private CafeteriaResponse response;

    @BeforeEach
    void setUp() {
        request = new CafeteriaRequest("NovaLanchonete", "novalanchonete@gmail.com", "12345678", Campus.CAMPUS_VII);
        response = new CafeteriaResponse("NovaLanchonete", "novalanchonete@gmail.com", true, "url/da/foto");
        mockCafeteria = Mockito.mock(Cafeteria.class);
        Mockito.when(mockCafeteria.getUsername()).thenReturn("admin_cafeteria");
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(mockCafeteria, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Deve criar uma nova cafeteria e retornar o token (201 Created)")
    void createCafeteria_ReturnsCreatedAndToken() throws Exception {
        String tokenEsperado = "jwt-token-valido";

        Mockito.when(cafeteriaService.createCafeteria(any(CafeteriaRequest.class))).thenReturn(mockCafeteria);
        Mockito.when(jwtService.generateToken(mockCafeteria)).thenReturn(tokenEsperado);

        mockMvc.perform(post("/cafeteria/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value(tokenEsperado));
    }

    @Test
    @DisplayName("Deve fazer upload de imagem com sucesso (200 OK)")
    void uploadImagem_ReturnsOkAndUrl() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "logo.png",
                MediaType.IMAGE_PNG_VALUE,
                "conteudo-da-imagem".getBytes()
        );
        String urlEsperada = "http://s3.aws.com/minha-imagem.png";

        Mockito.when(cafeteriaService.saveImage(any(Cafeteria.class), any())).thenReturn(urlEsperada);

        mockMvc.perform(multipart("/cafeteria/image")
                        .file(file)
                        .with(user(mockCafeteria))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(urlEsperada));
    }

    @Test
    @DisplayName("Deve atualizar uma cafeteria (200 OK)")
    void updateCafeteria_ReturnsUpdatedCafeteria() throws Exception {
        Mockito.when(cafeteriaService.updateCafeteria(eq("admin_cafeteria"), any(CafeteriaRequest.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/cafeteria/update")
                        .with(user(mockCafeteria))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve deletar uma cafeteria (200 OK)")
    void deleteCafeteria_ReturnsOk() throws Exception {
        mockMvc.perform(delete("/cafeteria/delete")
                        .with(user(mockCafeteria))
                        .with(csrf()))
                .andExpect(status().isOk());

        Mockito.verify(cafeteriaService).delete("admin_cafeteria");
    }

    @Test
    @DisplayName("Deve retornar os dados da cafeteria do usuário logado (200 OK)")
    void getMyCafeteria_ReturnsCafeteriaResponse() throws Exception {
        Mockito.when(cafeteriaService.getMyCafeteria("admin_cafeteria")).thenReturn(response);

        mockMvc.perform(get("/cafeteria/me")
                        .with(user(mockCafeteria)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve retornar uma lista de cafeterias públicas por campus (200 OK)")
    void getAllCafeterias_ReturnsList() throws Exception {
        Campus campus = Campus.CAMPUS_VII;
        List<CafeteriaResponse> responseList = List.of(response);

        Mockito.when(cafeteriaService.getCafeteriaByCampus(campus)).thenReturn(responseList);

        mockMvc.perform(get("/cafeteria/public/{campus}/all", campus))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("Deve retornar itens do menu de uma cafeteria pública (200 OK)")
    void getItems_ReturnsMenuItemList() throws Exception {
        String name = "CantinaCentral";
        Campus campus = Campus.CAMPUS_VII;
        List<MenuItemResponse> responseList = List.of(new MenuItemResponse(name, "Coxinha",
                "Muito boa", BigDecimal.valueOf(5), Category.SNACK, 10));

        Mockito.when(cafeteriaService.getItemsForCafeteria(name, campus)).thenReturn(responseList);

        mockMvc.perform(get("/cafeteria/public/{campus}/{name}", campus, name))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}