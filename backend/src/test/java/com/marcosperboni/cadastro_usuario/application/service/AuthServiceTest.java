package com.marcosperboni.cadastro_usuario.application.service;

import com.marcosperboni.cadastro_usuario.application.dto.ForgotPasswordRequest;
import com.marcosperboni.cadastro_usuario.application.dto.LoginRequest;
import com.marcosperboni.cadastro_usuario.application.dto.LoginResponse;
import com.marcosperboni.cadastro_usuario.application.dto.ResetPasswordRequest;
import com.marcosperboni.cadastro_usuario.application.exception.BusinessException;
import com.marcosperboni.cadastro_usuario.application.exception.InvalidCredentialsException;
import com.marcosperboni.cadastro_usuario.domain.model.PasswordResetToken;
import com.marcosperboni.cadastro_usuario.domain.model.Role;
import com.marcosperboni.cadastro_usuario.domain.model.Usuario;
import com.marcosperboni.cadastro_usuario.domain.repository.PasswordResetTokenRepository;
import com.marcosperboni.cadastro_usuario.domain.repository.UsuarioRepository;
import com.marcosperboni.cadastro_usuario.infrastructure.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PasswordResetTokenRepository tokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private Usuario usuario;

    @BeforeEach
    void setUp() throws Exception {
        usuario = new Usuario("Maria Silva", "(11) 91234-5678", "hash-senha", Role.USER);
        Field campoId = Usuario.class.getDeclaredField("id");
        campoId.setAccessible(true);
        campoId.set(usuario, 1L);

        ReflectionTestUtils.setField(authService, "expiracaoMinutos", 30L);
    }

    @Test
    void deveAutenticarComSucesso() {
        LoginRequest request = new LoginRequest("(11) 91234-5678", "senha1234");
        when(usuarioRepository.findByTelefone(request.telefone())).thenReturn(Optional.of(usuario));
        when(jwtService.gerarToken(usuario.getTelefone(), usuario.getRole().name(), usuario.getId()))
                .thenReturn("token-jwt");

        LoginResponse response = authService.login(request);

        assertThat(response.token()).isEqualTo("token-jwt");
        assertThat(response.nome()).isEqualTo("Maria Silva");
    }

    @Test
    void deveRejeitarCredenciaisInvalidas() {
        LoginRequest request = new LoginRequest("(11) 91234-5678", "senhaErrada");
        doThrow(new BadCredentialsException("invalido"))
                .when(authenticationManager).authenticate(any());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void deveGerarTokenDeRecuperacaoParaTelefoneExistente() {
        when(usuarioRepository.findByTelefone("(11) 91234-5678")).thenReturn(Optional.of(usuario));

        authService.solicitarRecuperacaoSenha(new ForgotPasswordRequest("(11) 91234-5678"));

        verify(tokenRepository).save(any(PasswordResetToken.class));
    }

    @Test
    void naoDeveFalharParaTelefoneInexistenteAoSolicitarRecuperacao() {
        when(usuarioRepository.findByTelefone("(00) 00000-0000")).thenReturn(Optional.empty());

        authService.solicitarRecuperacaoSenha(new ForgotPasswordRequest("(00) 00000-0000"));

        verify(tokenRepository, never()).save(any());
    }

    @Test
    void deveRedefinirSenhaComTokenValido() {
        PasswordResetToken token = new PasswordResetToken(usuario, "token-valido", Instant.now().plus(10, ChronoUnit.MINUTES));
        when(tokenRepository.findByToken("token-valido")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("novaSenha123")).thenReturn("nova-hash");

        authService.redefinirSenha(new ResetPasswordRequest("token-valido", "novaSenha123"));

        assertThat(usuario.getSenha()).isEqualTo("nova-hash");
        assertThat(token.isValido()).isFalse();
    }

    @Test
    void deveRejeitarTokenExpirado() {
        PasswordResetToken token = new PasswordResetToken(usuario, "token-expirado", Instant.now().minus(1, ChronoUnit.MINUTES));
        when(tokenRepository.findByToken("token-expirado")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> authService.redefinirSenha(new ResetPasswordRequest("token-expirado", "novaSenha123")))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void deveRejeitarTokenInexistente() {
        when(tokenRepository.findByToken("nao-existe")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.redefinirSenha(new ResetPasswordRequest("nao-existe", "novaSenha123")))
                .isInstanceOf(BusinessException.class);
    }
}
