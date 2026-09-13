package com.marcosperboni.cadastro_usuario.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcosperboni.cadastro_usuario.application.dto.ForgotPasswordRequest;
import com.marcosperboni.cadastro_usuario.application.dto.LoginRequest;
import com.marcosperboni.cadastro_usuario.application.dto.UsuarioRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TELEFONE_ADMIN_SEED = "(11) 99999-9999";
    private static final String SENHA_ADMIN_SEED = "Admin@123";

    @Test
    void deveLogarComCredenciaisValidas() throws Exception {
        LoginRequest login = new LoginRequest(TELEFONE_ADMIN_SEED, SENHA_ADMIN_SEED);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void deveRejeitarLoginComSenhaIncorreta() throws Exception {
        LoginRequest login = new LoginRequest(TELEFONE_ADMIN_SEED, "senhaErrada123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRejeitarLoginComCamposEmBranco() throws Exception {
        LoginRequest login = new LoginRequest("", "");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DirtiesContext
    void deveAceitarSolicitacaoDeRecuperacaoDeSenhaParaTelefoneExistente() throws Exception {
        UsuarioRequest cadastro = new UsuarioRequest("Carlos Souza", "(21) 97777-1111", "senhaSegura1");
        mockMvc.perform(post("/api/usuarios/registrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cadastro)));

        mockMvc.perform(post("/api/auth/esqueci-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ForgotPasswordRequest("(21) 97777-1111"))))
                .andExpect(status().isOk());
    }

    @Test
    void deveResponderComSucessoMesmoParaTelefoneInexistente() throws Exception {
        mockMvc.perform(post("/api/auth/esqueci-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ForgotPasswordRequest("(00) 00000-0000"))))
                .andExpect(status().isOk());
    }
}
