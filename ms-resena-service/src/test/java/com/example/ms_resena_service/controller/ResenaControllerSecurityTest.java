package com.example.ms_resena_service.controller;

import com.example.ms_resena_service.model.Resena;
import com.example.ms_resena_service.security.JwtAuthFilter;
import com.example.ms_resena_service.security.JwtService;
import com.example.ms_resena_service.security.SecurityConfig;
import com.example.ms_resena_service.service.ResenaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ResenaController.class)
@AutoConfigureMockMvc
@Import({
        SecurityConfig.class,
        JwtAuthFilter.class,
        ResenaControllerSecurityTest.HalTestConfig.class
})
class ResenaControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ResenaService resenaService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void userNoPuedeListarTodasLasResenas() throws Exception {
        configurarToken("token-user", "mavis", "USER");

        mockMvc.perform(get("/api/v1/resenas")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-user"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminPuedeListarTodasLasResenas() throws Exception {
        configurarToken("token-admin", "dracula", "ADMIN");
        when(resenaService.listarResenas()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/resenas")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-admin"))
                .andExpect(status().isOk());
    }

    @Test
    void userPuedeConsultarResenaPropia() throws Exception {
        configurarToken("token-user", "mavis", "USER");
        when(resenaService.buscarPorId(1L)).thenReturn(crearResena());

        mockMvc.perform(get("/api/v1/resenas/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void propiedadAjenaConservaForbidden() throws Exception {
        configurarToken("token-user", "mavis", "USER");
        when(resenaService.buscarPorId(1L))
                .thenThrow(new ResponseStatusException(
                        org.springframework.http.HttpStatus.FORBIDDEN,
                        "Reserva ajena"
                ));

        mockMvc.perform(get("/api/v1/resenas/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-user"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void userNoPuedeUsarFiltroGlobalPorHotel() throws Exception {
        configurarToken("token-user", "mavis", "USER");

        mockMvc.perform(get("/api/v1/resenas/hotel/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-user"))
                .andExpect(status().isForbidden());
    }

    @Test
    void solicitudSinTokenRecibeUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/resenas/1"))
                .andExpect(status().isUnauthorized());
    }

    private void configurarToken(String token, String nombre, String rol) {
        when(jwtService.extractNombre(token)).thenReturn(nombre);
        when(jwtService.extractRol(token)).thenReturn(rol);
        when(jwtService.isTokenValid(token)).thenReturn(true);
    }

    private Resena crearResena() {
        Resena resena = new Resena();
        resena.setId(1L);
        resena.setIdCliente(1L);
        resena.setIdHotel(1L);
        resena.setIdHabitacion(1L);
        resena.setIdReserva(1L);
        resena.setCalificacion(5);
        resena.setComentario("Excelente");
        resena.setEstadoResena("APROBADA");
        return resena;
    }

    @TestConfiguration
    @EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
    static class HalTestConfig {
    }
}
