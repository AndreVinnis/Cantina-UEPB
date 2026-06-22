package com.uepb.CoreService.controllers;

import com.uepb.CoreService.config.JwtService;
import com.uepb.CoreService.domain.Cafeteria;
import com.uepb.CoreService.dto.AuthRequest;
import com.uepb.CoreService.dto.AuthResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid AuthRequest data){
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.email(), data.password());
        var auth = authenticationManager.authenticate(usernamePassword);
        var token = jwtService.generateToken((Cafeteria) auth.getPrincipal());
        return ResponseEntity.ok(new AuthResponse(token));
    }
}
