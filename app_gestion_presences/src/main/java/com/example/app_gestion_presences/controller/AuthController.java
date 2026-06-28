package com.example.app_gestion_presences.controller;

import com.example.app_gestion_presences.dto.AuthResponse;
import com.example.app_gestion_presences.dto.LoginRequest;
import com.example.app_gestion_presences.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        // Côté client : supprimer le token JWT stocké
        return ResponseEntity.ok().build();
    }
}
