package com.marcosperboni.cadastro_usuario.application.service;

import com.marcosperboni.cadastro_usuario.application.dto.UsuarioRequest;
import com.marcosperboni.cadastro_usuario.application.dto.UsuarioResponse;
import com.marcosperboni.cadastro_usuario.application.dto.UsuarioRoleRequest;
import com.marcosperboni.cadastro_usuario.application.dto.UsuarioUpdateRequest;
import com.marcosperboni.cadastro_usuario.application.exception.BusinessException;
import com.marcosperboni.cadastro_usuario.application.exception.ResourceNotFoundException;
import com.marcosperboni.cadastro_usuario.domain.model.Role;
import com.marcosperboni.cadastro_usuario.domain.model.Usuario;
import com.marcosperboni.cadastro_usuario.domain.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UsuarioResponse registrar(UsuarioRequest request) {
        if (usuarioRepository.existsByTelefone(request.telefone())) {
            throw new BusinessException("Ja existe um usuario cadastrado com este telefone");
        }

        Usuario usuario = new Usuario(
                request.nome(),
                request.telefone(),
                passwordEncoder.encode(request.senha()),
                Role.USER
        );

        return UsuarioResponse.from(usuarioRepository.save(usuario));
    }

    @Transactional(readOnly = true)
    public Page<UsuarioResponse> listar(String busca, Pageable pageable) {
        String termo = busca == null ? "" : busca;
        return usuarioRepository
                .findByNomeContainingIgnoreCaseOrTelefoneContaining(termo, termo, pageable)
                .map(UsuarioResponse::from);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(Long id) {
        return UsuarioResponse.from(buscarEntidadePorId(id));
    }

    @Transactional
    public UsuarioResponse atualizar(Long id, UsuarioUpdateRequest request) {
        Usuario usuario = buscarEntidadePorId(id);

        boolean telefoneAlterado = !usuario.getTelefone().equals(request.telefone());
        if (telefoneAlterado && usuarioRepository.existsByTelefone(request.telefone())) {
            throw new BusinessException("Ja existe um usuario cadastrado com este telefone");
        }

        usuario.atualizarDados(request.nome(), request.telefone());
        return UsuarioResponse.from(usuario);
    }

    @Transactional
    public UsuarioResponse alterarRole(Long id, UsuarioRoleRequest request) {
        Usuario usuario = buscarEntidadePorId(id);
        usuario.alterarRole(request.role());
        return UsuarioResponse.from(usuario);
    }

    @Transactional
    public void excluir(Long id) {
        Usuario usuario = buscarEntidadePorId(id);
        usuarioRepository.delete(usuario);
    }

    private Usuario buscarEntidadePorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado: " + id));
    }
}
