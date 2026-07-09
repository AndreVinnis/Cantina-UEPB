package com.uepb.CoreService.controllers;

import com.uepb.CoreService.config.JwtService;
import com.uepb.CoreService.domain.Cafeteria;
import com.uepb.CoreService.dto.response.AuthResponse;
import com.uepb.CoreService.dto.request.CafeteriaRequest;
import com.uepb.CoreService.dto.response.CafeteriaResponse;
import com.uepb.CoreService.dto.response.MenuItemResponse;
import com.uepb.CoreService.enums.Campus;
import com.uepb.CoreService.services.CafeteriaService;
import com.uepb.CoreService.services.MenuItemService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/cafeteria")
public class CafeteriaController {

    @Autowired
    private CafeteriaService cafeteriaService;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/create")
    public ResponseEntity<AuthResponse> create(@RequestBody @Valid CafeteriaRequest newCafeteria){
        Cafeteria cafeteria = cafeteriaService.createCafeteria(newCafeteria);
        var token = jwtService.generateToken(cafeteria);
        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(token));
    }

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadImagem(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("image") MultipartFile image) {

        String urlImagem = cafeteriaService.saveImage((Cafeteria) userDetails, image);
        return ResponseEntity.ok(urlImagem);
    }

    @PatchMapping("/update")
    public ResponseEntity<CafeteriaResponse> updateCafeteria(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid CafeteriaRequest newCafeteria
    ){
        return ResponseEntity.ok(cafeteriaService.updateCafeteria(userDetails.getUsername(), newCafeteria));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteCafeteria(@AuthenticationPrincipal UserDetails userDetails){
        cafeteriaService.delete(userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<CafeteriaResponse> getMyCafeteria(@AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(cafeteriaService.getMyCafeteria(userDetails.getUsername()));
    }

    @GetMapping("/public/{campus}/all")
    public ResponseEntity<List<CafeteriaResponse>> getAllCafeterias(@PathVariable Campus campus){
        return ResponseEntity.ok(cafeteriaService.getCafeteriaByCampus(campus));
    }

    @GetMapping("/public/{campus}/{name}")
    public ResponseEntity<List<MenuItemResponse>> getItems(@PathVariable String name, @PathVariable Campus campus){
        return ResponseEntity.ok(cafeteriaService.getItemsForCafeteria(name, campus));
    }
}
