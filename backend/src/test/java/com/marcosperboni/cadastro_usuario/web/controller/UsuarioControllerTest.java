package com.marcosperboni.cadastro_usuario.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcosperboni.cadastro_usuario.application.dto.*;
import com.marcosperboni.cadastro_usuario.domain.model.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TELEFONE_ADMIN_SEED = "(11) 99999-9999";
    private static final String SENHA_ADMIN_SEED = "Admin@123";

    private String obterToken(String telefone, String senha) throws Exception {
        LoginRequest login = new LoginRequest(telefone, senha);
        String responseJson = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(responseJson).get("token").asText();
    }

    private long registrarUsuario(String nome, String telefone, String senha) throws Exception {
        UsuarioRequest request = new UsuarioRequest(nome, telefone, senha);
        String responseJson = mockMvc.perform(post("/api/usuarios/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(responseJson).get("id").asLong();
    }

    @Test
    void deveRegistrarNovoUsuario() throws Exception {
        UsuarioRequest request = new UsuarioRequest("Ana Paula", "(31) 98765-4321", "senhaForte1");

        mockMvc.perform(post("/api/usuarios/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Ana Paula"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void deveRejeitarRegistroComTelefoneInvalido() throws Exception {
        UsuarioRequest request = new UsuarioRequest("Ana Paula", "telefone-invalido", "senhaForte1");

        mockMvc.perform(post("/api/usuarios/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRejeitarRegistroComSenhaFraca() throws Exception {
        UsuarioRequest request = new UsuarioRequest("Ana Paula", "(31) 98765-4321", "123");

        mockMvc.perform(post("/api/usuarios/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRejeitarRegistroComTelefoneDuplicado() throws Exception {
        registrarUsuario("Bruno Lima", "(31) 91111-2222", "senhaForte1");
        UsuarioRequest duplicado = new UsuarioRequest("Outro Nome", "(31) 91111-2222", "outraSenha1");

        mockMvc.perform(post("/api/usuarios/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicado)))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRejeitarListagemSemAutenticacao() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveRejeitarListagemParaUsuarioComum() throws Exception {
        registrarUsuario("Carla Nunes", "(41) 93333-4444", "senhaForte1");
        String token = obterToken("(41) 93333-4444", "senhaForte1");

        mockMvc.perform(get("/api/usuarios").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveListarUsuariosParaAdmin() throws Exception {
        String tokenAdmin = obterToken(TELEFONE_ADMIN_SEED, SENHA_ADMIN_SEED);

        mockMvc.perform(get("/api/usuarios").header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void deveRetornarPerfilProprioComGetMe() throws Exception {
        registrarUsuario("Diego Alves", "(51) 95555-6666", "senhaForte1");
        String token = obterToken("(51) 95555-6666", "senhaForte1");

        mockMvc.perform(get("/api/usuarios/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.telefone").value("(51) 95555-6666"));
    }

    @Test
    void deveImpedirUsuarioComumDeAcessarDadosDeOutroUsuario() throws Exception {
        long idUsuario1 = registrarUsuario("Eduarda Melo", "(61) 96666-7777", "senhaForte1");
        registrarUsuario("Fabio Costa", "(61) 97777-8888", "senhaForte1");
        String tokenFabio = obterToken("(61) 97777-8888", "senhaForte1");

        mockMvc.perform(get("/api/usuarios/" + idUsuario1).header("Authorization", "Bearer " + tokenFabio))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveAtualizarProprioCadastro() throws Exception {
        long id = registrarUsuario("Gustavo Reis", "(71) 91234-0000", "senhaForte1");
        String token = obterToken("(71) 91234-0000", "senhaForte1");

        UsuarioUpdateRequest atualizacao = new UsuarioUpdateRequest("Gustavo Reis Junior", "(71) 91234-0000");

        mockMvc.perform(put("/api/usuarios/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(atualizacao)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Gustavo Reis Junior"));
    }

    @Test
    void deveRetornarNotFoundAoBuscarUsuarioInexistente() throws Exception {
        String tokenAdmin = obterToken(TELEFONE_ADMIN_SEED, SENHA_ADMIN_SEED);

        mockMvc.perform(get("/api/usuarios/999999").header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveAlterarRoleApenasComoAdmin() throws Exception {
        long id = registrarUsuario("Helena Prado", "(81) 91234-1111", "senhaForte1");
        String tokenHelena = obterToken("(81) 91234-1111", "senhaForte1");
        String tokenAdmin = obterToken(TELEFONE_ADMIN_SEED, SENHA_ADMIN_SEED);

        UsuarioRoleRequest novaRole = new UsuarioRoleRequest(Role.ADMIN);

        mockMvc.perform(patch("/api/usuarios/" + id + "/role")
                        .header("Authorization", "Bearer " + tokenHelena)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(novaRole)))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/usuarios/" + id + "/role")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(novaRole)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void deveExcluirUsuarioApenasComoAdmin() throws Exception {
        long id = registrarUsuario("Igor Tavares", "(91) 91234-2222", "senhaForte1");
        String tokenAdmin = obterToken(TELEFONE_ADMIN_SEED, SENHA_ADMIN_SEED);

        mockMvc.perform(delete("/api/usuarios/" + id).header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/usuarios/" + id).header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveImpedirUsuarioComumDeExcluir() throws Exception {
        long id = registrarUsuario("Julia Farias", "(11) 92222-3333", "senhaForte1");
        String token = obterToken("(11) 92222-3333", "senhaForte1");

        mockMvc.perform(delete("/api/usuarios/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
}
