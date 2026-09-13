package com.marcosperboni.cadastro_usuario.application.dto;

import com.marcosperboni.cadastro_usuario.domain.model.Role;
import jakarta.validation.constraints.NotNull;

public record UsuarioRoleRequest(
        @NotNull(message = "Perfil e obrigatorio")
        Role role
) {
}
