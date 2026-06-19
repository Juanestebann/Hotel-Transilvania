package com.example.ms_reserva_service.controller;

import com.example.ms_reserva_service.model.Reserva;
import com.example.ms_reserva_service.security.JwtService;
import com.example.ms_reserva_service.service.ReservaService;
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

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservaController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ReservaControllerTest.HalTestConfig.class)
class ReservaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservaService service;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void deberiaRetornarReservaPorId() throws Exception {

        Reserva reserva = crearReserva();

        when(service.findById(1L))
                .thenReturn(reserva);

        mockMvc.perform(get("/api/v1/reservas/1")
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.estadoReserva").value("PENDIENTE"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.todas-las-reservas.href").exists())
                .andExpect(jsonPath("$._links.actualizar-reserva.href").exists())
                .andExpect(jsonPath("$._links.eliminar-reserva.href").exists());

        verify(service).findById(1L);
    }

    @Test
    void deberiaCrearReserva() throws Exception {

        Reserva reserva = crearReserva();

        when(service.guardar(any(Reserva.class)))
                .thenReturn(reserva);

        String json = """
                {
                    "idCliente": 1,
                    "idUsuario": 1,
                    "idHotel": 1,
                    "idHabitacion": 1,
                    "fechaInicio": "2026-06-20",
                    "fechaFin": "2026-06-22",
                    "cantidadPersonas": 2,
                    "estadoReserva": "PENDIENTE"
                }
                """;

        mockMvc.perform(post("/api/v1/reservas")
                        .contentType(MediaTypes.HAL_JSON)
                        .accept(MediaTypes.HAL_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.idCliente").value(1))
                .andExpect(jsonPath("$.estadoReserva").value("PENDIENTE"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.crear-reserva.href").exists())
                .andExpect(jsonPath("$._links.cambiar-estado.href").exists());

        verify(service).guardar(any(Reserva.class));
    }

    @Test
    void deberiaRetornar400CuandoFaltanCamposObligatorios() throws Exception {

        String json = """
                {
                    "idCliente": 1
                }
                """;

        mockMvc.perform(post("/api/v1/reservas")
                        .contentType(MediaTypes.HAL_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    private Reserva crearReserva() {
        Reserva reserva = new Reserva();
        reserva.setId(1L);
        reserva.setIdCliente(1L);
        reserva.setIdUsuario(1L);
        reserva.setIdHotel(1L);
        reserva.setIdHabitacion(1L);
        reserva.setFechaInicio(LocalDate.of(2026, 6, 20));
        reserva.setFechaFin(LocalDate.of(2026, 6, 22));
        reserva.setCantidadPersonas(2);
        reserva.setEstadoReserva("PENDIENTE");
        return reserva;
    }

    @TestConfiguration
    @EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
    static class HalTestConfig {
    }
}
