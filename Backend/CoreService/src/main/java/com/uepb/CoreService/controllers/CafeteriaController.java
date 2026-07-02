package com.uepb.CoreService.controllers;

import com.uepb.CoreService.config.JwtService;
import com.uepb.CoreService.domain.Cafeteria;
import com.uepb.CoreService.dto.response.AuthResponse;
import com.uepb.CoreService.dto.request.CafeteriaRequest;
import com.uepb.CoreService.services.CafeteriaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/cafeteria")
public class CafeteriaController {

    @Autowired
    CafeteriaService cafeteriaService;

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
}
