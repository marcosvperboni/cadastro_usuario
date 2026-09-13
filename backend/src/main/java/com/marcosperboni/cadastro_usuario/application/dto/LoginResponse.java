package com.marcosperboni.cadastro_usuario.application.dto;

import com.marcosperboni.cadastro_usuario.domain.model.Role;

public record LoginResponse(
        String token,
        String tipo,
        Long usuarioId,
        String nome,
        Role role
) {
    public static LoginResponse of(String token, Long usuarioId, String nome, Role role) {
        return new LoginResponse(token, "Bearer", usuarioId, nome, role);
    }
}
