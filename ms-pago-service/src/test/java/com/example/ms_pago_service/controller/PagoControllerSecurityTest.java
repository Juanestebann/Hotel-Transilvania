package com.example.ms_pago_service.controller;

import com.example.ms_pago_service.model.Pago;
import com.example.ms_pago_service.security.JwtAuthFilter;
import com.example.ms_pago_service.security.JwtService;
import com.example.ms_pago_service.security.SecurityConfig;
import com.example.ms_pago_service.service.PagoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PagoController.class)
@AutoConfigureMockMvc
@Import({
        SecurityConfig.class,
        JwtAuthFilter.class,
        PagoControllerSecurityTest.HalTestConfig.class
})
class PagoControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PagoService pagoService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void userPuedeConsultarPagoPropioPorId() throws Exception {
        configurarToken("token-user", "mavis", "USER");
        when(pagoService.findById(1L)).thenReturn(crearPago());

        mockMvc.perform(get("/api/v1/pagos/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPago").value(1));
    }

    @Test
    void userNoPuedeConsultarPagoAjenoPorId() throws Exception {
        configurarToken("token-user", "mavis", "USER");
        when(pagoService.findById(1L))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Pago ajeno"
                ));

        mockMvc.perform(get("/api/v1/pagos/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-user"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void adminPuedeConsultarCualquierPagoPorId() throws Exception {
        configurarToken("token-admin", "dracula", "ADMIN");
        when(pagoService.findById(1L)).thenReturn(crearPago());

        mockMvc.perform(get("/api/v1/pagos/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-admin"))
                .andExpect(status().isOk());
    }

    @Test
    void userPuedeConsultarPagosDeReservaPropia() throws Exception {
        configurarToken("token-user", "mavis", "USER");
        when(pagoService.findByReservaId(1L)).thenReturn(List.of(crearPago()));

        mockMvc.perform(get("/api/v1/pagos/reserva/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idUsuario").value(1));
    }

    @Test
    void userNoPuedeConsultarPagosDeReservaAjena() throws Exception {
        configurarToken("token-user", "mavis", "USER");
        when(pagoService.findByReservaId(1L))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Reserva ajena"
                ));

        mockMvc.perform(get("/api/v1/pagos/reserva/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-user"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void adminPuedeConsultarPagosDeCualquierReserva() throws Exception {
        configurarToken("token-admin", "dracula", "ADMIN");
        when(pagoService.findByReservaId(1L)).thenReturn(List.of(crearPago()));

        mockMvc.perform(get("/api/v1/pagos/reserva/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-admin"))
                .andExpect(status().isOk());
    }

    @Test
    void solicitudSinTokenRecibeUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/pagos/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userNoPuedeListarTodosLosPagos() throws Exception {
        configurarToken("token-user", "mavis", "USER");

        mockMvc.perform(get("/api/v1/pagos")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-user"))
                .andExpect(status().isForbidden());
    }

    private void configurarToken(String token, String nombre, String rol) {
        when(jwtService.extractNombre(token)).thenReturn(nombre);
        when(jwtService.extractRol(token)).thenReturn(rol);
        when(jwtService.isTokenValid(token)).thenReturn(true);
    }

    private Pago crearPago() {
        Pago pago = new Pago();
        pago.setIdPago(1L);
        pago.setReservaId(1L);
        pago.setIdUsuario(1L);
        pago.setMonto(new BigDecimal("50000"));
        pago.setMetodoPago("TARJETA");
        pago.setEstadoPago("APROBADO");
        pago.setFechaPago(LocalDateTime.of(2026, 6, 21, 20, 0));
        return pago;
    }

    @TestConfiguration
    @EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
    static class HalTestConfig {
    }
}
