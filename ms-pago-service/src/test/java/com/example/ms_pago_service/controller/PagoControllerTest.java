package com.example.ms_pago_service.controller;

import com.example.ms_pago_service.model.Pago;
import com.example.ms_pago_service.security.JwtService;
import com.example.ms_pago_service.service.PagoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PagoController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(PagoControllerTest.HalTestConfig.class)
class PagoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PagoService pagoService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void deberiaRetornarPagoPorId() throws Exception {
        Pago pago = crearPago();

        when(pagoService.findById(1L)).thenReturn(pago);

        mockMvc.perform(get("/api/v1/pagos/1")
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPago").value(1))
                .andExpect(jsonPath("$.reservaId").value(1))
                .andExpect(jsonPath("$.idUsuario").value(1))
                .andExpect(jsonPath("$.metodoPago").value("TARJETA"))
                .andExpect(jsonPath("$.estadoPago").value("APROBADO"))
                .andExpect(jsonPath("$._links.self.href").exists());

        verify(pagoService).findById(1L);
    }

    @Test
    void deberiaCrearPago() throws Exception {

        Pago pago = crearPago();

        when(pagoService.save(any(Pago.class)))
                .thenReturn(pago);

        String json = """
                {
                    "reservaId": 1,
                    "idUsuario": 1,
                    "monto": 50000,
                    "metodoPago": "TARJETA",
                    "estadoPago": "APROBADO",
                    "fechaPago": "2026-06-21T20:00:00"
                }
                """;

        mockMvc.perform(post("/api/v1/pagos")
                        .contentType(MediaTypes.HAL_JSON)
                        .accept(MediaTypes.HAL_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idPago").value(1))
                .andExpect(jsonPath("$.reservaId").value(1))
                .andExpect(jsonPath("$.estadoPago").value("APROBADO"))
                .andExpect(jsonPath("$._links.self.href").exists());

        verify(pagoService).save(any(Pago.class));
    }

    @Test
    void deberiaRetornar400CuandoFaltanCamposObligatorios() throws Exception {

        String json = """
                {
                    "reservaId": 1
                }
                """;

        mockMvc.perform(post("/api/v1/pagos")
                        .contentType(MediaTypes.HAL_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
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