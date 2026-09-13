package com.marcosperboni.cadastro_usuario.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    private final SecretKey chave;
    private final long expiracaoMs;

    public JwtService(
            @Value("${app.security.jwt.secret}") String segredo,
            @Value("${app.security.jwt.expiracao-ms}") long expiracaoMs
    ) {
        this.chave = Keys.hmacShaKeyFor(segredo.getBytes());
        this.expiracaoMs = expiracaoMs;
    }

    public String gerarToken(String telefone, String role, Long usuarioId) {
        Date agora = new Date();
        Date expiracao = new Date(agora.getTime() + expiracaoMs);
        return Jwts.builder()
                .subject(telefone)
                .claim("role", role)
                .claim("usuarioId", usuarioId)
                .issuedAt(agora)
                .expiration(expiracao)
                .signWith(chave)
                .compact();
    }

    public String extrairTelefone(String token) {
        return extrairClaim(token, Claims::getSubject);
    }

    public boolean tokenValido(String token, String telefoneEsperado) {
        String telefone = extrairTelefone(token);
        return telefone.equals(telefoneEsperado) && !tokenExpirado(token);
    }

    private boolean tokenExpirado(String token) {
        return extrairClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extrairClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parser()
                .verifyWith(chave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return resolver.apply(claims);
    }
}
