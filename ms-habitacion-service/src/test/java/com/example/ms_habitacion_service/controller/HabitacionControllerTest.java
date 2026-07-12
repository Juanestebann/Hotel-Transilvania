package com.example.ms_habitacion_service.controller;

import com.example.ms_habitacion_service.model.Habitacion;
import com.example.ms_habitacion_service.security.JwtService;
import com.example.ms_habitacion_service.service.HabitacionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

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

@WebMvcTest(HabitacionController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(HabitacionControllerTest.HalTestConfig.class)
class HabitacionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HabitacionService service;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void deberiaListarHabitaciones() throws Exception {
        when(service.findAll()).thenReturn(List.of(crearHabitacion(1L), crearHabitacion(2L)));

        mockMvc.perform(get("/api/v1/habitaciones")
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idHabitacion").value(1))
                .andExpect(jsonPath("$[0].links[0].rel").value("self"));

        verify(service).findAll();
        verifyNoMoreInteractions(service);
    }

    @Test
    void deberiaRetornarHabitacionPorId() throws Exception {
        when(service.findById(1L)).thenReturn(crearHabitacion(1L));

        mockMvc.perform(get("/api/v1/habitaciones/1")
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idHabitacion").value(1))
                .andExpect(jsonPath("$.numeroHabitacion").value("101"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.todas-las-habitaciones.href").exists())
                .andExpect(jsonPath("$._links.actualizar-habitacion.href").exists())
                .andExpect(jsonPath("$._links.eliminar-habitacion.href").exists());

        verify(service).findById(1L);
        verifyNoMoreInteractions(service);
    }

    @Test
    void buscarHabitacionInexistenteRetorna404() throws Exception {
        when(service.findById(99L))
                .thenThrow(new NoSuchElementException("Habitación no encontrada con id: 99"));

        mockMvc.perform(get("/api/v1/habitaciones/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Habitación no encontrada con id: 99"));

        verify(service).findById(99L);
        verifyNoMoreInteractions(service);
    }

    @Test
    void deberiaFiltrarPorEstado() throws Exception {
        when(service.findByEstadoHabitacion("DISPONIBLE")).thenReturn(List.of(crearHabitacion(1L)));

        mockMvc.perform(get("/api/v1/habitaciones/estado/DISPONIBLE")
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estadoHabitacion").value("DISPONIBLE"))
                .andExpect(jsonPath("$[0].links[2].rel").value("buscar-por-estado"));

        verify(service).findByEstadoHabitacion("DISPONIBLE");
        verifyNoMoreInteractions(service);
    }

    @Test
    void estadoInvalidoRetorna400() throws Exception {
        when(service.findByEstadoHabitacion("BLOQUEADA"))
                .thenThrow(new IllegalArgumentException("Estado de habitación inválido"));

        mockMvc.perform(get("/api/v1/habitaciones/estado/BLOQUEADA"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Estado de habitación inválido"));

        verify(service).findByEstadoHabitacion("BLOQUEADA");
        verifyNoMoreInteractions(service);
    }

    @Test
    void deberiaFiltrarPorCapacidad() throws Exception {
        when(service.findByCapacidadMinima(3)).thenReturn(List.of(crearHabitacion(1L)));

        mockMvc.perform(get("/api/v1/habitaciones/capacidad/3")
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].capacidad").value(2));

        verify(service).findByCapacidadMinima(3);
        verifyNoMoreInteractions(service);
    }

    @Test
    void deberiaFiltrarPorHotel() throws Exception {
        when(service.findByIdHotel(1L)).thenReturn(List.of(crearHabitacion(1L)));

        mockMvc.perform(get("/api/v1/habitaciones/hotel/1")
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idHotel").value(1));

        verify(service).findByIdHotel(1L);
        verifyNoMoreInteractions(service);
    }

    @Test
    void deberiaCrearHabitacion() throws Exception {
        when(service.guardar(any(Habitacion.class))).thenReturn(crearHabitacion(1L));

        mockMvc.perform(post("/api/v1/habitaciones")
                        .contentType(MediaTypes.HAL_JSON)
                        .accept(MediaTypes.HAL_JSON)
                        .content(jsonHabitacion()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idHabitacion").value(1))
                .andExpect(jsonPath("$.numeroHabitacion").value("101"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.crear-habitacion.href").exists())
                .andExpect(jsonPath("$._links.buscar-por-hotel.href").exists());

        verify(service).guardar(any(Habitacion.class));
        verifyNoMoreInteractions(service);
    }

    @Test
    void crearHabitacionConHotelInexistenteRetorna404() throws Exception {
        when(service.guardar(any(Habitacion.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Hotel no encontrado"));

        mockMvc.perform(post("/api/v1/habitaciones")
                        .contentType(MediaTypes.HAL_JSON)
                        .content(jsonHabitacion()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Hotel no encontrado"));

        verify(service).guardar(any(Habitacion.class));
        verifyNoMoreInteractions(service);
    }

    @Test
    void crearHabitacionConHotelNoDisponibleRetorna503() throws Exception {
        when(service.guardar(any(Habitacion.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Hotel-service no disponible"));

        mockMvc.perform(post("/api/v1/habitaciones")
                        .contentType(MediaTypes.HAL_JSON)
                        .content(jsonHabitacion()))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("Hotel-service no disponible"));

        verify(service).guardar(any(Habitacion.class));
        verifyNoMoreInteractions(service);
    }

    @Test
    void crearHabitacionConTimeoutRetorna504() throws Exception {
        when(service.guardar(any(Habitacion.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT, "Timeout consultando hotel-service"));

        mockMvc.perform(post("/api/v1/habitaciones")
                        .contentType(MediaTypes.HAL_JSON)
                        .content(jsonHabitacion()))
                .andExpect(status().isGatewayTimeout())
                .andExpect(jsonPath("$.message").value("Timeout consultando hotel-service"));

        verify(service).guardar(any(Habitacion.class));
        verifyNoMoreInteractions(service);
    }

    @Test
    void deberiaRetornar400CuandoFaltanCamposObligatorios() throws Exception {
        String json = """
                {
                    "numeroHabitacion": "101"
                }
                """;

        mockMvc.perform(post("/api/v1/habitaciones")
                        .contentType(MediaTypes.HAL_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deberiaActualizarHabitacion() throws Exception {
        Habitacion actualizada = crearHabitacion(1L);
        actualizada.setNumeroHabitacion("202");
        when(service.actualizar(any(Long.class), any(Habitacion.class))).thenReturn(actualizada);

        mockMvc.perform(put("/api/v1/habitaciones/1")
                        .contentType(MediaTypes.HAL_JSON)
                        .accept(MediaTypes.HAL_JSON)
                        .content(jsonHabitacion()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idHabitacion").value(1))
                .andExpect(jsonPath("$.numeroHabitacion").value("202"));

        verify(service).actualizar(any(Long.class), any(Habitacion.class));
        verifyNoMoreInteractions(service);
    }

    @Test
    void actualizarHabitacionInexistenteRetorna404() throws Exception {
        when(service.actualizar(any(Long.class), any(Habitacion.class)))
                .thenThrow(new NoSuchElementException("Habitación no encontrada con id: 99"));

        mockMvc.perform(put("/api/v1/habitaciones/99")
                        .contentType(MediaTypes.HAL_JSON)
                        .content(jsonHabitacion()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Habitación no encontrada con id: 99"));

        verify(service).actualizar(any(Long.class), any(Habitacion.class));
        verifyNoMoreInteractions(service);
    }

    @Test
    void deberiaEliminarHabitacion() throws Exception {
        mockMvc.perform(delete("/api/v1/habitaciones/1"))
                .andExpect(status().isNoContent());

        verify(service).eliminar(1L);
        verifyNoMoreInteractions(service);
    }

    @Test
    void eliminarHabitacionInexistenteRetorna404() throws Exception {
        doThrow(new NoSuchElementException("Habitación no encontrada con id: 99"))
                .when(service).eliminar(99L);

        mockMvc.perform(delete("/api/v1/habitaciones/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Habitación no encontrada con id: 99"));

        verify(service).eliminar(99L);
        verifyNoMoreInteractions(service);
    }

    private Habitacion crearHabitacion(Long id) {
        Habitacion habitacion = new Habitacion();
        habitacion.setIdHabitacion(id);
        habitacion.setNumeroHabitacion("101");
        habitacion.setTipoHabitacion("DOBLE");
        habitacion.setPrecioBase(75000.0);
        habitacion.setCapacidad(2);
        habitacion.setEstadoHabitacion("DISPONIBLE");
        habitacion.setIdHotel(1L);
        return habitacion;
    }

    private String jsonHabitacion() {
        return """
                {
                    "numeroHabitacion": "101",
                    "tipoHabitacion": "DOBLE",
                    "precioBase": 75000,
                    "capacidad": 2,
                    "estadoHabitacion": "DISPONIBLE",
                    "idHotel": 1
                }
                """;
    }

    @TestConfiguration
    @EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
    static class HalTestConfig {
    }
}
