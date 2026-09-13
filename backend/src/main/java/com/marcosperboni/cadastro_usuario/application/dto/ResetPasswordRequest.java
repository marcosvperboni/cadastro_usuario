package com.marcosperboni.cadastro_usuario.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ResetPasswordRequest(
        @NotBlank(message = "Token e obrigatorio")
        String token,

        @NotBlank(message = "Nova senha e obrigatoria")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$",
                message = "Senha deve ter no minimo 8 caracteres, com letras e numeros"
        )
        String novaSenha
) {
}
