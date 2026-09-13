package com.marcosperboni.cadastro_usuario.application.service;

import com.marcosperboni.cadastro_usuario.application.dto.ForgotPasswordRequest;
import com.marcosperboni.cadastro_usuario.application.dto.LoginRequest;
import com.marcosperboni.cadastro_usuario.application.dto.LoginResponse;
import com.marcosperboni.cadastro_usuario.application.dto.ResetPasswordRequest;
import com.marcosperboni.cadastro_usuario.application.exception.BusinessException;
import com.marcosperboni.cadastro_usuario.application.exception.InvalidCredentialsException;
import com.marcosperboni.cadastro_usuario.domain.model.PasswordResetToken;
import com.marcosperboni.cadastro_usuario.domain.model.Usuario;
import com.marcosperboni.cadastro_usuario.domain.repository.PasswordResetTokenRepository;
import com.marcosperboni.cadastro_usuario.domain.repository.UsuarioRepository;
import com.marcosperboni.cadastro_usuario.infrastructure.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${app.security.reset-token.expiracao-minutos}")
    private long expiracaoMinutos;

    public AuthService(
            AuthenticationManager authenticationManager,
            UsuarioRepository usuarioRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.telefone(), request.senha())
            );
        } catch (BadCredentialsException ex) {
            throw new InvalidCredentialsException("Telefone ou senha invalidos");
        }

        Usuario usuario = usuarioRepository.findByTelefone(request.telefone())
                .orElseThrow(() -> new InvalidCredentialsException("Telefone ou senha invalidos"));

        String token = jwtService.gerarToken(usuario.getTelefone(), usuario.getRole().name(), usuario.getId());
        return LoginResponse.of(token, usuario.getId(), usuario.getNome(), usuario.getRole());
    }

    @Transactional
    public void solicitarRecuperacaoSenha(ForgotPasswordRequest request) {
        usuarioRepository.findByTelefone(request.telefone()).ifPresent(usuario -> {
            String token = UUID.randomUUID().toString();
            Instant expiraEm = Instant.now().plus(expiracaoMinutos, ChronoUnit.MINUTES);
            tokenRepository.save(new PasswordResetToken(usuario, token, expiraEm));

            // Nao ha integracao de e-mail/SMS neste projeto de portfolio.
            // Em producao, este token seria enviado por e-mail ou SMS ao usuario.
            log.info("Token de redefinicao de senha gerado para o telefone {}: {}", usuario.getTelefone(), token);
        });
        // Resposta sempre identica independente do telefone existir, para evitar enumeracao de usuarios.
    }

    @Transactional
    public void redefinirSenha(ResetPasswordRequest request) {
        PasswordResetToken resetToken = tokenRepository.findByToken(request.token())
                .orElseThrow(() -> new BusinessException("Token invalido ou expirado"));

        if (!resetToken.isValido()) {
            throw new BusinessException("Token invalido ou expirado");
        }

        Usuario usuario = resetToken.getUsuario();
        usuario.atualizarSenha(passwordEncoder.encode(request.novaSenha()));
        resetToken.marcarComoUsado();
    }
}
