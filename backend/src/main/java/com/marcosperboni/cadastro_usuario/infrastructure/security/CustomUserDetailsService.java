package com.marcosperboni.cadastro_usuario.infrastructure.security;

import com.marcosperboni.cadastro_usuario.domain.model.Usuario;
import com.marcosperboni.cadastro_usuario.domain.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String telefone) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByTelefone(telefone)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado: " + telefone));
        return new CustomUserDetails(usuario);
    }
}
