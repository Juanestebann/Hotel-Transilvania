package com.example.ms_auth_usuarios_service.controller;
import com.example.ms_auth_usuarios_service.service.CustomUserDetailsService;
import com.example.ms_auth_usuarios_service.model.Usuario;
import com.example.ms_auth_usuarios_service.security.JwtService;
import com.example.ms_auth_usuarios_service.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(AuthControllerTest.HalTestConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UsuarioService usuarioService;

    @Test
    void deberiaRegistrarUsuario() throws Exception {
        Usuario usuario = crearUsuario();

        when(usuarioService.register(any(Usuario.class)))
                .thenReturn(usuario);

        String json = """
                {
                    "nombre": "JuanAdmin",
                    "password": "Admin2026*"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaTypes.HAL_JSON)
                        .accept(MediaTypes.HAL_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Usuario registrado correctamente"))
                .andExpect(jsonPath("$.idUsuario").value(1))
                .andExpect(jsonPath("$.nombre").value("JuanAdmin"))
                .andExpect(jsonPath("$.rol").value("USER"))
                .andExpect(jsonPath("$._links.login").exists())
                .andExpect(jsonPath("$._links.validate").exists())
                .andExpect(jsonPath("$._links.usuario").exists());

        verify(usuarioService).register(any(Usuario.class));
    }

    @Test
    void deberiaHacerLoginCorrectamente() throws Exception {
        Usuario usuario = crearUsuario();

        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        when(authentication.isAuthenticated())
                .thenReturn(true);

        when(usuarioService.findByNombre("JuanAdmin"))
                .thenReturn(Optional.of(usuario));

        when(jwtService.generateToken("JuanAdmin", "USER"))
                .thenReturn("token-test");

        String json = """
                {
                    "nombre": "JuanAdmin",
                    "password": "Admin2026*"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaTypes.HAL_JSON)
                        .accept(MediaTypes.HAL_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-test"))
                .andExpect(jsonPath("$.nombre").value("JuanAdmin"))
                .andExpect(jsonPath("$.rol").value("USER"))
                .andExpect(jsonPath("$._links.validate").exists())
                .andExpect(jsonPath("$._links.usuario").exists());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(usuarioService).findByNombre("JuanAdmin");
        verify(jwtService).generateToken("JuanAdmin", "USER");
    }

    @Test
    void deberiaValidarToken() throws Exception {
        mockMvc.perform(get("/api/v1/auth/validate")
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$._links.login").exists())
                .andExpect(jsonPath("$._links.register").exists());
    }

    private Usuario crearUsuario() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1L);
        usuario.setNombre("JuanAdmin");
        usuario.setPassword("Admin2026*");
        usuario.setRol("USER");
        return usuario;
    }

    @TestConfiguration
    @EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
    static class HalTestConfig {
    }
    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;
}