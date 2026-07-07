package com.example.ms_reserva_service.controller;

import com.example.ms_reserva_service.model.Reserva;
import com.example.ms_reserva_service.security.JwtAuthFilter;
import com.example.ms_reserva_service.security.JwtService;
import com.example.ms_reserva_service.security.SecurityConfig;
import com.example.ms_reserva_service.service.ReservaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.NoSuchElementException;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservaController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, JwtAuthFilter.class})
class ReservaInternalSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservaService reservaService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void userNoPuedeCambiarEstadoInternamente() throws Exception {
        configurarToken("token-user", "mavis", "USER");

        mockMvc.perform(put("/api/v1/reservas/internal/1/estado")
                        .param("estadoReserva", "CONFIRMADA")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-user"))
                .andExpect(status().isForbidden());
    }

    @Test
    void pagoServicePuedeConfirmarReservaInternamente() throws Exception {
        configurarToken("token-service", "ms-pago-service", "SERVICE");
        Reserva reserva = reservaConfirmada();
        when(reservaService.cambiarEstadoInterno(1L, "CONFIRMADA"))
                .thenReturn(reserva);

        mockMvc.perform(put("/api/v1/reservas/internal/1/estado")
                        .param("estadoReserva", "CONFIRMADA")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-service"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.estadoReserva").value("CONFIRMADA"));

        verify(reservaService).cambiarEstadoInterno(1L, "CONFIRMADA");
    }

    @Test
    void otroServiceNoPuedeCambiarEstadoInternamente() throws Exception {
        configurarToken("token-other", "ms-reserva-report-service", "SERVICE");

        mockMvc.perform(put("/api/v1/reservas/internal/1/estado")
                        .param("estadoReserva", "CONFIRMADA")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-other"))
                .andExpect(status().isForbidden());
    }

    @Test
    void endpointInternoSinTokenRecibeUnauthorized() throws Exception {
        mockMvc.perform(put("/api/v1/reservas/internal/1/estado")
                        .param("estadoReserva", "CONFIRMADA"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void reservaInexistenteRecibeNotFound() throws Exception {
        configurarToken("token-service", "ms-pago-service", "SERVICE");
        when(reservaService.cambiarEstadoInterno(99L, "CONFIRMADA"))
                .thenThrow(new NoSuchElementException("Reserva no encontrada con id: 99"));

        mockMvc.perform(put("/api/v1/reservas/internal/99/estado")
                        .param("estadoReserva", "CONFIRMADA")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-service"))
                .andExpect(status().isNotFound());
    }

    private void configurarToken(String token, String nombre, String rol) {
        when(jwtService.extractNombre(token)).thenReturn(nombre);
        when(jwtService.extractRol(token)).thenReturn(rol);
        when(jwtService.isTokenValid(token)).thenReturn(true);
    }

    private Reserva reservaConfirmada() {
        Reserva reserva = new Reserva();
        reserva.setId(1L);
        reserva.setIdUsuario(1L);
        reserva.setEstadoReserva("CONFIRMADA");
        return reserva;
    }
}
