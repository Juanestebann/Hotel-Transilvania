package com.example.ms_disponibilidad_service.controller;

import com.example.ms_disponibilidad_service.model.Disponibilidad;
import com.example.ms_disponibilidad_service.security.JwtAuthFilter;
import com.example.ms_disponibilidad_service.security.JwtService;
import com.example.ms_disponibilidad_service.security.SecurityConfig;
import com.example.ms_disponibilidad_service.service.DisponibilidadService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DisponibilidadController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, JwtAuthFilter.class})
class DisponibilidadControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DisponibilidadService disponibilidadService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void userNoPuedeActualizarDisponibilidadDirectamente() throws Exception {
        configurarToken("token-user", "mavis", "USER");

        mockMvc.perform(put("/api/v1/disponibilidades/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(disponibilidadCompletaJson()))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminPuedeActualizarDisponibilidadDirectamente() throws Exception {
        configurarToken("token-admin", "dracula", "ADMIN");
        Disponibilidad actualizada = crearDisponibilidad("OCUPADA");
        when(disponibilidadService.actualizar(eq(1L), any(Disponibilidad.class)))
                .thenReturn(actualizada);

        mockMvc.perform(put("/api/v1/disponibilidades/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(disponibilidadCompletaJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("OCUPADA"));
    }

    @Test
    void userNoPuedeActualizarEndpointInterno() throws Exception {
        configurarToken("token-user", "mavis", "USER");

        mockMvc.perform(put("/api/v1/disponibilidades/internal/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(estadoJson()))
                .andExpect(status().isForbidden());
    }

    @Test
    void reservaServicePuedeActualizarEndpointInterno() throws Exception {
        configurarToken("token-service", "ms-reserva-service", "SERVICE");
        Disponibilidad actualizada = crearDisponibilidad("OCUPADA");
        when(disponibilidadService.actualizarEstadoInterno(1L, "OCUPADA"))
                .thenReturn(actualizada);

        mockMvc.perform(put("/api/v1/disponibilidades/internal/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-service")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(estadoJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.estado").value("OCUPADA"));

        verify(disponibilidadService).actualizarEstadoInterno(1L, "OCUPADA");
    }

    @Test
    void otroServiceNoPuedeActualizarEndpointInterno() throws Exception {
        configurarToken("token-other-service", "ms-pago-service", "SERVICE");

        mockMvc.perform(put("/api/v1/disponibilidades/internal/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-other-service")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(estadoJson()))
                .andExpect(status().isForbidden());
    }

    @Test
    void endpointInternoSinTokenRecibeUnauthorized() throws Exception {
        mockMvc.perform(put("/api/v1/disponibilidades/internal/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(estadoJson()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void disponibilidadInexistenteRecibeNotFound() throws Exception {
        configurarToken("token-service", "ms-reserva-service", "SERVICE");
        when(disponibilidadService.actualizarEstadoInterno(99L, "OCUPADA"))
                .thenThrow(new NoSuchElementException("Disponibilidad no encontrada con id: 99"));

        mockMvc.perform(put("/api/v1/disponibilidades/internal/99")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-service")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(estadoJson()))
                .andExpect(status().isNotFound());
    }

    private void configurarToken(String token, String nombre, String rol) {
        when(jwtService.extractNombre(token)).thenReturn(nombre);
        when(jwtService.extractRol(token)).thenReturn(rol);
        when(jwtService.isTokenValid(token)).thenReturn(true);
    }

    private Disponibilidad crearDisponibilidad(String estado) {
        Disponibilidad disponibilidad = new Disponibilidad();
        disponibilidad.setId(1L);
        disponibilidad.setIdHabitacion(1L);
        disponibilidad.setFecha(LocalDate.of(2026, 6, 20));
        disponibilidad.setEstado(estado);
        return disponibilidad;
    }

    private String disponibilidadCompletaJson() {
        return """
                {
                    "idHabitacion": 1,
                    "fecha": "2026-06-20",
                    "estado": "OCUPADA"
                }
                """;
    }

    private String estadoJson() {
        return """
                {"estado": "OCUPADA"}
                """;
    }
}
