package com.marcosperboni.cadastro_usuario.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "password_reset_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, unique = true, length = 100)
    private String token;

    @Column(name = "expira_em", nullable = false)
    private Instant expiraEm;

    @Column(nullable = false)
    private boolean usado;

    public PasswordResetToken(Usuario usuario, String token, Instant expiraEm) {
        this.usuario = usuario;
        this.token = token;
        this.expiraEm = expiraEm;
        this.usado = false;
    }

    public boolean isValido() {
        return !usado && Instant.now().isBefore(expiraEm);
    }

    public void marcarComoUsado() {
        this.usado = true;
    }
}
