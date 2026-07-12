package com.example.ms_cliente_service.controller;

import com.example.ms_cliente_service.dto.ClienteValidacionDTO;
import com.example.ms_cliente_service.model.Cliente;
import com.example.ms_cliente_service.security.JwtService;
import com.example.ms_cliente_service.service.ClienteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
    void deberiaListarClientes() throws Exception {
        when(service.findAll()).thenReturn(List.of(crearCliente(1L), crearCliente(2L)));

        mockMvc.perform(get("/api/v1/clientes")
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].links[0].rel").value("self"));

        verify(service).findAll();
        verifyNoMoreInteractions(service);
    }

    @Test
    void deberiaRetornarClientePorId() throws Exception {
        when(service.findById(1L)).thenReturn(crearCliente(1L));

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
        verifyNoMoreInteractions(service);
    }

    @Test
    void buscarClienteInexistenteRetorna404() throws Exception {
        when(service.findById(99L))
                .thenThrow(new NoSuchElementException("Cliente no encontrado con id: 99"));

        mockMvc.perform(get("/api/v1/clientes/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cliente no encontrado con id: 99"));

        verify(service).findById(99L);
        verifyNoMoreInteractions(service);
    }

    @Test
    void deberiaBuscarClientePorRut() throws Exception {
        when(service.findByRutDocumento("11111111-1")).thenReturn(crearCliente(1L));

        mockMvc.perform(get("/api/v1/clientes/rut/11111111-1")
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rutDocumento").value("11111111-1"))
                .andExpect(jsonPath("$._links.buscar-por-rut.href").exists());

        verify(service).findByRutDocumento("11111111-1");
        verifyNoMoreInteractions(service);
    }

    @Test
    void buscarClientePorRutInexistenteRetorna404() throws Exception {
        when(service.findByRutDocumento("99999999-9"))
                .thenThrow(new NoSuchElementException("Cliente no encontrado con rutDocumento: 99999999-9"));

        mockMvc.perform(get("/api/v1/clientes/rut/99999999-9"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cliente no encontrado con rutDocumento: 99999999-9"));

        verify(service).findByRutDocumento("99999999-9");
        verifyNoMoreInteractions(service);
    }

    @Test
    void deberiaFiltrarPorRolCliente() throws Exception {
        when(service.findByRolCliente("ADMIN")).thenReturn(List.of(crearCliente(1L)));

        mockMvc.perform(get("/api/v1/clientes")
                        .param("rolCliente", "ADMIN")
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rolCliente").value("ADMIN"))
                .andExpect(jsonPath("$[0].links[6].rel").value("buscar-por-rol"));

        verify(service).findByRolCliente("ADMIN");
        verifyNoMoreInteractions(service);
    }

    @Test
    void deberiaFiltrarPorTipoCliente() throws Exception {
        when(service.findByTipoCliente("FRECUENTE")).thenReturn(List.of(crearCliente(1L)));

        mockMvc.perform(get("/api/v1/clientes")
                        .param("tipoCliente", "FRECUENTE")
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipoCliente").value("FRECUENTE"))
                .andExpect(jsonPath("$[0].links[7].rel").value("buscar-por-tipo"));

        verify(service).findByTipoCliente("FRECUENTE");
        verifyNoMoreInteractions(service);
    }

    @Test
    void deberiaValidarClienteExistente() throws Exception {
        when(service.validarExistenciaCliente(1L)).thenReturn(new ClienteValidacionDTO(1L, true));

        mockMvc.perform(get("/api/v1/clientes/1/validacion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCliente").value(1))
                .andExpect(jsonPath("$.existe").value(true))
                .andExpect(jsonPath("$.rutDocumento").doesNotExist());

        verify(service).validarExistenciaCliente(1L);
        verifyNoMoreInteractions(service);
    }

    @Test
    void validarClienteInexistenteRetorna404() throws Exception {
        when(service.validarExistenciaCliente(99L))
                .thenThrow(new NoSuchElementException("Cliente no encontrado con id: 99"));

        mockMvc.perform(get("/api/v1/clientes/99/validacion"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cliente no encontrado con id: 99"));

        verify(service).validarExistenciaCliente(99L);
        verifyNoMoreInteractions(service);
    }

    @Test
    void deberiaCrearCliente() throws Exception {
        when(service.guardar(any(Cliente.class))).thenReturn(crearCliente(1L));

        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaTypes.HAL_JSON)
                        .accept(MediaTypes.HAL_JSON)
                        .content(jsonCliente()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.rutDocumento").value("11111111-1"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.crear.href").exists())
                .andExpect(jsonPath("$._links.buscar-por-rut.href").exists());

        verify(service).guardar(any(Cliente.class));
        verifyNoMoreInteractions(service);
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

    @Test
    void deberiaActualizarCliente() throws Exception {
        Cliente actualizado = crearCliente(1L);
        actualizado.setTelefono("+56922223333");
        when(service.actualizar(any(Long.class), any(Cliente.class))).thenReturn(actualizado);

        mockMvc.perform(put("/api/v1/clientes/1")
                        .contentType(MediaTypes.HAL_JSON)
                        .accept(MediaTypes.HAL_JSON)
                        .content(jsonCliente()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.telefono").value("+56922223333"));

        verify(service).actualizar(any(Long.class), any(Cliente.class));
        verifyNoMoreInteractions(service);
    }

    @Test
    void actualizarClienteInexistenteRetorna404() throws Exception {
        when(service.actualizar(any(Long.class), any(Cliente.class)))
                .thenThrow(new NoSuchElementException("Cliente no encontrado con id: 99"));

        mockMvc.perform(put("/api/v1/clientes/99")
                        .contentType(MediaTypes.HAL_JSON)
                        .content(jsonCliente()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cliente no encontrado con id: 99"));

        verify(service).actualizar(any(Long.class), any(Cliente.class));
        verifyNoMoreInteractions(service);
    }

    @Test
    void deberiaEliminarCliente() throws Exception {
        mockMvc.perform(delete("/api/v1/clientes/1"))
                .andExpect(status().isNoContent());

        verify(service).eliminar(1L);
        verifyNoMoreInteractions(service);
    }

    @Test
    void eliminarClienteInexistenteRetorna404() throws Exception {
        doThrow(new NoSuchElementException("Cliente no encontrado con id: 99"))
                .when(service).eliminar(99L);

        mockMvc.perform(delete("/api/v1/clientes/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cliente no encontrado con id: 99"));

        verify(service).eliminar(99L);
        verifyNoMoreInteractions(service);
    }

    private Cliente crearCliente(Long id) {
        Cliente cliente = new Cliente();
        cliente.setId(id);
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

    @TestConfiguration
    @EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
    static class HalTestConfig {
    }
}
