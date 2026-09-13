package com.marcosperboni.cadastro_usuario.application.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Telefone e obrigatorio")
        String telefone,

        @NotBlank(message = "Senha e obrigatoria")
        String senha
) {
}
