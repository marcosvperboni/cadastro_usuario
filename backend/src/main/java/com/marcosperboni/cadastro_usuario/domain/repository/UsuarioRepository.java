package com.marcosperboni.cadastro_usuario.domain.repository;

import com.marcosperboni.cadastro_usuario.domain.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByTelefone(String telefone);

    boolean existsByTelefone(String telefone);

    Page<Usuario> findByNomeContainingIgnoreCaseOrTelefoneContaining(String nome, String telefone, Pageable pageable);
}
