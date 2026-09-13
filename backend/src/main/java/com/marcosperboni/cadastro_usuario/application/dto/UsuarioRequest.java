package com.marcosperboni.cadastro_usuario.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(

        @NotBlank(message = "Nome e obrigatorio")
        @Size(min = 3, max = 120, message = "Nome deve ter entre 3 e 120 caracteres")
        String nome,

        @NotBlank(message = "Telefone e obrigatorio")
        @Pattern(regexp = "^\\(?\\d{2}\\)?[\\s-]?\\d{4,5}-?\\d{4}$", message = "Telefone invalido. Use o formato (11) 91234-5678")
        String telefone,

        @NotBlank(message = "Senha e obrigatoria")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$",
                message = "Senha deve ter no minimo 8 caracteres, com letras e numeros"
        )
        String senha
) {
}
