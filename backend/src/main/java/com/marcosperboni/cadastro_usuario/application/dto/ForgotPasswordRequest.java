package com.marcosperboni.cadastro_usuario.application.dto;

import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(
        @NotBlank(message = "Telefone e obrigatorio")
        String telefone
) {
}
