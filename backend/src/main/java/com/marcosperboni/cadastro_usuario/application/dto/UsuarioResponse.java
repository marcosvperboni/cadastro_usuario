package com.marcosperboni.cadastro_usuario.application.dto;

import com.marcosperboni.cadastro_usuario.domain.model.Role;
import com.marcosperboni.cadastro_usuario.domain.model.Usuario;

import java.time.Instant;

public record UsuarioResponse(
        Long id,
        String nome,
        String telefone,
        Role role,
        boolean ativo,
        Instant criadoEm
) {
    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getTelefone(),
                usuario.getRole(),
                usuario.isAtivo(),
                usuario.getCriadoEm()
        );
    }
}
