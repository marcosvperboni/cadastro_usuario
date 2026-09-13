package com.marcosperboni.cadastro_usuario.web.controller;

import com.marcosperboni.cadastro_usuario.application.dto.ForgotPasswordRequest;
import com.marcosperboni.cadastro_usuario.application.dto.LoginRequest;
import com.marcosperboni.cadastro_usuario.application.dto.LoginResponse;
import com.marcosperboni.cadastro_usuario.application.dto.ResetPasswordRequest;
import com.marcosperboni.cadastro_usuario.application.service.AuthService;
import jakarta.validation.Valid;
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
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/esqueci-senha")
    public ResponseEntity<Void> esqueciSenha(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.solicitarRecuperacaoSenha(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/redefinir-senha")
    public ResponseEntity<Void> redefinirSenha(@Valid @RequestBody ResetPasswordRequest request) {
        authService.redefinirSenha(request);
        return ResponseEntity.ok().build();
    }
}
