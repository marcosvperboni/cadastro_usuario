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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuarioExistente;

    @BeforeEach
    void setUp() throws Exception {
        usuarioExistente = new Usuario("Maria Silva", "(11) 91234-5678", "hash-senha", Role.USER);
        setId(usuarioExistente, 1L);
    }

    private void setId(Usuario usuario, Long id) throws Exception {
        Field campoId = Usuario.class.getDeclaredField("id");
        campoId.setAccessible(true);
        campoId.set(usuario, id);
    }

    @Test
    void deveRegistrarUsuarioComSucesso() {
        UsuarioRequest request = new UsuarioRequest("Joao Souza", "(11) 98888-7777", "senha1234");
        when(usuarioRepository.existsByTelefone(request.telefone())).thenReturn(false);
        when(passwordEncoder.encode(request.senha())).thenReturn("senha-criptografada");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            setId(u, 10L);
            return u;
        });

        UsuarioResponse response = usuarioService.registrar(request);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.nome()).isEqualTo("Joao Souza");
        assertThat(response.role()).isEqualTo(Role.USER);
        verify(passwordEncoder).encode("senha1234");
    }

    @Test
    void deveRejeitarRegistroComTelefoneJaCadastrado() {
        UsuarioRequest request = new UsuarioRequest("Joao Souza", "(11) 91234-5678", "senha1234");
        when(usuarioRepository.existsByTelefone(request.telefone())).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.registrar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Ja existe um usuario");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deveListarUsuariosComPaginacao() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Usuario> pagina = new PageImpl<>(List.of(usuarioExistente), pageable, 1);
        when(usuarioRepository.findByNomeContainingIgnoreCaseOrTelefoneContaining(anyString(), anyString(), any()))
                .thenReturn(pagina);

        Page<UsuarioResponse> resultado = usuarioService.listar("Maria", pageable);

        assertThat(resultado.getTotalElements()).isEqualTo(1);
        assertThat(resultado.getContent().get(0).nome()).isEqualTo("Maria Silva");
    }

    @Test
    void deveBuscarUsuarioPorId() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioExistente));

        UsuarioResponse response = usuarioService.buscarPorId(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.telefone()).isEqualTo("(11) 91234-5678");
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoEncontrado() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.buscarPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deveAtualizarDadosDoUsuario() {
        UsuarioUpdateRequest request = new UsuarioUpdateRequest("Maria Silva Santos", "(11) 91234-5678");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioExistente));

        UsuarioResponse response = usuarioService.atualizar(1L, request);

        assertThat(response.nome()).isEqualTo("Maria Silva Santos");
    }

    @Test
    void deveRejeitarAtualizacaoComTelefoneDeOutroUsuario() {
        UsuarioUpdateRequest request = new UsuarioUpdateRequest("Maria Silva", "(11) 90000-0000");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioExistente));
        when(usuarioRepository.existsByTelefone("(11) 90000-0000")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.atualizar(1L, request))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void deveAlterarRoleDoUsuario() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioExistente));

        UsuarioResponse response = usuarioService.alterarRole(1L, new UsuarioRoleRequest(Role.ADMIN));

        assertThat(response.role()).isEqualTo(Role.ADMIN);
    }

    @Test
    void deveExcluirUsuario() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioExistente));

        usuarioService.excluir(1L);

        verify(usuarioRepository).delete(usuarioExistente);
    }

    @Test
    void deveLancarExcecaoAoExcluirUsuarioInexistente() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.excluir(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(usuarioRepository, never()).delete(any());
    }
}
