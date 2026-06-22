package com.uepb.CoreService.controllers;

import com.uepb.CoreService.config.JwtService;
import com.uepb.CoreService.domain.Cafeteria;
import com.uepb.CoreService.dto.AuthResponse;
import com.uepb.CoreService.dto.CafeteriaRequest;
import com.uepb.CoreService.services.CafeteriaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
