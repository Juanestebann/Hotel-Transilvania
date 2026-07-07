package com.example.ms_auth_usuarios_service.controller;

import com.example.ms_auth_usuarios_service.model.Usuario;
import com.example.ms_auth_usuarios_service.security.JwtAuthFilter;
import com.example.ms_auth_usuarios_service.security.JwtService;
import com.example.ms_auth_usuarios_service.security.SecurityConfig;
import com.example.ms_auth_usuarios_service.service.CustomUserDetailsService;
import com.example.ms_auth_usuarios_service.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsuarioController.class)
@AutoConfigureMockMvc
@Import({
        SecurityConfig.class,
        JwtAuthFilter.class,
        UsuarioControllerSecurityTest.HalTestConfig.class
})
class UsuarioControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void userPuedeConsultarSuIdentidadSinPassword() throws Exception {
        configurarToken("token-user", "usuario1", "USER");
        when(usuarioService.findAuthenticatedByNombre("usuario1"))
                .thenReturn(crearUsuario(2L, "usuario1", "USER"));

        mockMvc.perform(get("/api/v1/usuarios/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario").value(2))
                .andExpect(jsonPath("$.nombre").value("usuario1"))
                .andExpect(jsonPath("$.rol").value("USER"))
                .andExpect(jsonPath("$.password").doesNotExist());

        verify(usuarioService).findAuthenticatedByNombre("usuario1");
    }

    @Test
    void adminPuedeConsultarSuIdentidadSinPassword() throws Exception {
        configurarToken("token-admin", "admin1", "ADMIN");
        when(usuarioService.findAuthenticatedByNombre("admin1"))
                .thenReturn(crearUsuario(1L, "admin1", "ADMIN"));

        mockMvc.perform(get("/api/v1/usuarios/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario").value(1))
                .andExpect(jsonPath("$.nombre").value("admin1"))
                .andExpect(jsonPath("$.rol").value("ADMIN"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void solicitudSinTokenRecibeUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userNoPuedeConsultarUsuarioPorIdAdministrativamente() throws Exception {
        configurarToken("token-user", "usuario1", "USER");

        mockMvc.perform(get("/api/v1/usuarios/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-user"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminPuedeConsultarUsuarioPorIdSinExponerPassword() throws Exception {
        configurarToken("token-admin", "admin1", "ADMIN");
        when(usuarioService.findById(2L))
                .thenReturn(crearUsuario(2L, "usuario1", "USER"));

        mockMvc.perform(get("/api/v1/usuarios/2")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario").value(2))
                .andExpect(jsonPath("$.nombre").value("usuario1"))
                .andExpect(jsonPath("$.password").doesNotExist());

        verify(usuarioService).findById(2L);
    }

    private void configurarToken(String token, String nombre, String rol) {
        when(jwtService.extractNombre(token)).thenReturn(nombre);
        when(jwtService.isTokenValid(token)).thenReturn(true);
        when(customUserDetailsService.loadUserByUsername(nombre))
                .thenReturn(User.withUsername(nombre)
                        .password("password-test")
                        .roles(rol)
                        .build());
    }

    private Usuario crearUsuario(Long id, String nombre, String rol) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(id);
        usuario.setNombre(nombre);
        usuario.setPassword("$2a$10$hash-que-no-debe-salir");
        usuario.setRol(rol);
        return usuario;
    }

    @TestConfiguration
    @EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
    static class HalTestConfig {
    }
}
