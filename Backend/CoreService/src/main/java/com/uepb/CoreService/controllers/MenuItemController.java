package com.uepb.CoreService.controllers;

import com.uepb.CoreService.domain.Cafeteria;
import com.uepb.CoreService.dto.request.MenuItemRequest;
import com.uepb.CoreService.dto.response.MenuItemResponse;
import com.uepb.CoreService.services.MenuItemService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/menuItem")
public class MenuItemController {

    @Autowired
    private MenuItemService menuItemService;

    @PostMapping("/create")
    public ResponseEntity<MenuItemResponse> create(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid MenuItemRequest newMenuItem
    ){

        MenuItemResponse item = menuItemService.createMenuItem((Cafeteria) userDetails, newMenuItem);
        return ResponseEntity.ok(item);
    }

    @PostMapping(value = "/my/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadImagem(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("name") String name,
            @RequestParam("image") MultipartFile image
    ) {

        String urlImagem = menuItemService.saveImage((Cafeteria) userDetails, name, image);
        return ResponseEntity.ok(urlImagem);
    }
}
