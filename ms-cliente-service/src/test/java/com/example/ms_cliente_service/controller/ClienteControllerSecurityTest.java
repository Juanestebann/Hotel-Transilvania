package com.example.ms_cliente_service.controller;

import com.example.ms_cliente_service.dto.ClienteValidacionDTO;
import com.example.ms_cliente_service.security.JwtAuthFilter;
import com.example.ms_cliente_service.security.JwtService;
import com.example.ms_cliente_service.security.SecurityConfig;
import com.example.ms_cliente_service.service.ClienteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.hateoas.MediaTypes;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.NoSuchElementException;

import com.example.ms_cliente_service.model.Cliente;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;

@WebMvcTest(ClienteController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, JwtAuthFilter.class})
class ClienteControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteService clienteService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void userPuedeValidarClienteConRespuestaMinima() throws Exception {
        configurarToken("token-user", "mavis", "USER");
        when(clienteService.validarExistenciaCliente(1L))
                .thenReturn(new ClienteValidacionDTO(1L, true));

        mockMvc.perform(get("/api/v1/clientes/1/validacion")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCliente").value(1))
                .andExpect(jsonPath("$.existe").value(true))
                .andExpect(jsonPath("$.rutDocumento").doesNotExist())
                .andExpect(jsonPath("$.telefono").doesNotExist())
                .andExpect(jsonPath("$.direccion").doesNotExist());

        verify(clienteService).validarExistenciaCliente(1L);
    }

    @Test
    void adminPuedeValidarCliente() throws Exception {
        configurarToken("token-admin", "dracula", "ADMIN");
        when(clienteService.validarExistenciaCliente(1L))
                .thenReturn(new ClienteValidacionDTO(1L, true));

        mockMvc.perform(get("/api/v1/clientes/1/validacion")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCliente").value(1))
                .andExpect(jsonPath("$.existe").value(true));
    }

    @Test
    void solicitudSinTokenRecibeUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/clientes/1/validacion"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rolNoPermitidoRecibeForbidden() throws Exception {
        configurarToken("token-guest", "invitado", "GUEST");

        mockMvc.perform(get("/api/v1/clientes/1/validacion")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-guest"))
                .andExpect(status().isForbidden());
    }

    @Test
    void clienteInexistenteRecibeNotFound() throws Exception {
        configurarToken("token-user", "mavis", "USER");
        when(clienteService.validarExistenciaCliente(99L))
                .thenThrow(new NoSuchElementException("Cliente no encontrado con id: 99"));

        mockMvc.perform(get("/api/v1/clientes/99/validacion")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-user"))
                .andExpect(status().isNotFound());
    }

    @Test
    void userNoPuedeConsultarEndpointAdministrativoPorId() throws Exception {
        configurarToken("token-user", "mavis", "USER");

        mockMvc.perform(get("/api/v1/clientes/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-user"))
                .andExpect(status().isForbidden());
    }

    @Test
    void userNoPuedeListarClientes() throws Exception {
        configurarToken("token-user", "mavis", "USER");

        mockMvc.perform(get("/api/v1/clientes")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-user"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminPuedeListarClientes() throws Exception {
        configurarToken("token-admin", "dracula", "ADMIN");
        when(clienteService.findAll()).thenReturn(List.of(crearCliente()));

        mockMvc.perform(get("/api/v1/clientes")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(clienteService).findAll();
    }

    @Test
    void adminPuedeCrearCliente() throws Exception {
        configurarToken("token-admin", "dracula", "ADMIN");
        when(clienteService.guardar(any(Cliente.class))).thenReturn(crearCliente());

        mockMvc.perform(post("/api/v1/clientes")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-admin")
                        .contentType(MediaTypes.HAL_JSON)
                        .content(jsonCliente()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(clienteService).guardar(any(Cliente.class));
    }

    @Test
    void tokenInvalidoRecibeUnauthorized() throws Exception {
        when(jwtService.extractNombre("token-invalido"))
                .thenThrow(new IllegalArgumentException("Token inválido"));

        mockMvc.perform(get("/api/v1/clientes/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-invalido"))
                .andExpect(status().isUnauthorized());
    }

    private void configurarToken(String token, String nombre, String rol) {
        when(jwtService.extractNombre(token)).thenReturn(nombre);
        when(jwtService.extractRol(token)).thenReturn(rol);
        when(jwtService.isTokenValid(token)).thenReturn(true);
    }

    private Cliente crearCliente() {
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setRutDocumento("11111111-1");
        cliente.setTelefono("+56912345678");
        cliente.setDireccion("Santiago");
        cliente.setRolCliente("ADMIN");
        cliente.setTipoCliente("FRECUENTE");
        return cliente;
    }

    private String jsonCliente() {
        return """
                {
                    "rutDocumento": "11111111-1",
                    "telefono": "+56912345678",
                    "direccion": "Santiago",
                    "rolCliente": "ADMIN",
                    "tipoCliente": "FRECUENTE"
                }
                """;
    }
}
