package com.example.ms_cliente_service.controller;

import com.example.ms_cliente_service.model.Cliente;
import com.example.ms_cliente_service.security.JwtService;
import com.example.ms_cliente_service.service.ClienteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.MediaTypes;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClienteController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ClienteControllerTest.HalTestConfig.class)
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteService service;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void deberiaRetornarClientePorId() throws Exception {

        Cliente cliente = crearCliente();

        when(service.findById(1L))
                .thenReturn(cliente);

        mockMvc.perform(get("/api/v1/clientes/1")
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.rutDocumento").value("11111111-1"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.listar-todos.href").exists())
                .andExpect(jsonPath("$._links.actualizar.href").exists())
                .andExpect(jsonPath("$._links.eliminar.href").exists());

        verify(service).findById(1L);
    }

    @Test
    void deberiaCrearCliente() throws Exception {

        Cliente cliente = crearCliente();

        when(service.guardar(any(Cliente.class)))
                .thenReturn(cliente);

        String json = """
                {
                    "rutDocumento": "11111111-1",
                    "telefono": "+56912345678",
                    "direccion": "Santiago",
                    "rolCliente": "ADMIN",
                    "tipoCliente": "FRECUENTE"
                }
                """;

        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaTypes.HAL_JSON)
                        .accept(MediaTypes.HAL_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.rutDocumento").value("11111111-1"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.crear.href").exists())
                .andExpect(jsonPath("$._links.buscar-por-rut.href").exists());

        verify(service).guardar(any(Cliente.class));
    }

    @Test
    void deberiaRetornar400CuandoFaltanCamposObligatorios() throws Exception {

        String json = """
                {
                    "rutDocumento": "11111111-1"
                }
                """;

        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaTypes.HAL_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
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

    @TestConfiguration
    @EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
    static class HalTestConfig {
    }
}
