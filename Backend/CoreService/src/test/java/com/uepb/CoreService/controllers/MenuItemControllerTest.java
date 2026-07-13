package com.uepb.CoreService.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uepb.CoreService.config.JwtService;
import com.uepb.CoreService.domain.Cafeteria;
import com.uepb.CoreService.dto.request.ChangeStockRequest;
import com.uepb.CoreService.dto.request.MenuItemRequest;
import com.uepb.CoreService.dto.response.MenuItemResponse;
import com.uepb.CoreService.enums.AvailabilityMode;
import com.uepb.CoreService.enums.Category;
import com.uepb.CoreService.services.AuthorizationService;
import com.uepb.CoreService.services.MenuItemService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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

@WebMvcTest(MenuItemController.class)
@AutoConfigureMockMvc(addFilters = false)
public class MenuItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MenuItemService menuItemService;

    @MockBean
    private AuthorizationService authorizationService;

    @MockBean
    private JwtService jwtService;

    private Cafeteria mockCafeteria;
    private MenuItemResponse response;
    private MenuItemRequest request;

    @BeforeEach
    void setUp() {
        request = new MenuItemRequest(
                "Coxinha",
                "Coxinha de frango",
                new BigDecimal("5.50"),
                Category.SNACK,
                AvailabilityMode.INVENTORY_CONTROL,
                30
        );

        response = new MenuItemResponse(
                "Cantina Central",
                "Coxinha",
                "Coxinha de frango",
                new BigDecimal("5.50"),
                Category.SNACK,
                30
        );

        mockCafeteria = Mockito.mock(Cafeteria.class);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(mockCafeteria, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testCreateMenuItem() throws Exception {
        Mockito.when(menuItemService.createMenuItem(any(Cafeteria.class), any(MenuItemRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/menuItem/create")
                        .with(user(mockCafeteria))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testUploadImagem() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "imagem.png",
                MediaType.IMAGE_PNG_VALUE,
                "dados-da-imagem".getBytes()
        );
        String expectedUrl = "http://url-da-imagem.com/imagem.png";

        Mockito.when(menuItemService.saveImage(any(Cafeteria.class), eq("Coxinha"), any()))
                .thenReturn(expectedUrl);

        mockMvc.perform(multipart("/menuItem/item/image")
                        .file(image)
                        .param("name", "Coxinha")
                        .with(user(mockCafeteria))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedUrl));
    }

    @Test
    void testGetAllItems() throws Exception {
        List<MenuItemResponse> responseList = List.of(response);

        Mockito.when(menuItemService.getAllMyItems(any(Cafeteria.class)))
                .thenReturn(responseList);

        mockMvc.perform(get("/menuItem/me/items/all")
                        .with(user(mockCafeteria)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testAddStock() throws Exception {
        Mockito.when(menuItemService.addStock(any(Cafeteria.class), eq("Coxinha"), eq(10)))
                .thenReturn(response);

        mockMvc.perform(post("/menuItem/addStock")
                        .with(user(mockCafeteria))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testRemoveStock() throws Exception {
        Mockito.when(menuItemService.removeStock(any(Cafeteria.class), eq("Coxinha"), eq(5)))
                .thenReturn(response);

        mockMvc.perform(post("/menuItem/removeStock")
                        .with(user(mockCafeteria))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testBlockMenuItem() throws Exception {
        Mockito.when(menuItemService.changeAvailability(any(Cafeteria.class), eq("Coxinha"), eq(false)))
                .thenReturn(response);

        mockMvc.perform(patch("/menuItem/Cafe/lockItem")
                        .with(user(mockCafeteria))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void testUnblockMenuItem() throws Exception {
        Mockito.when(menuItemService.changeAvailability(any(Cafeteria.class), eq("Coxinha"), eq(true)))
                .thenReturn(response);

        mockMvc.perform(patch("/menuItem/Cafe/unlockItem")
                        .with(user(mockCafeteria))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdateItem() throws Exception {
        Mockito.when(menuItemService.updateItem(any(Cafeteria.class), eq("Coxinha"), any(MenuItemRequest.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/menuItem/Cafe/update")
                        .with(user(mockCafeteria))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteItem() throws Exception {
        mockMvc.perform(delete("/menuItem/Coxinha/delete")
                        .with(user(mockCafeteria))
                        .with(csrf()))
                .andExpect(status().isOk());

        Mockito.verify(menuItemService, Mockito.times(1)).deleteItem(any(Cafeteria.class), eq("Coxinha"));
    }
}