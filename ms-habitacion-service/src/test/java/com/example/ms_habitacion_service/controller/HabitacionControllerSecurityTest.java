package com.example.ms_habitacion_service.controller;

import com.example.ms_habitacion_service.model.Habitacion;
import com.example.ms_habitacion_service.security.JwtService;
import com.example.ms_habitacion_service.service.HabitacionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,"
                + "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration,"
                + "org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration",
        "eureka.client.enabled=false"
})
class HabitacionControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HabitacionService habitacionService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void getHabitacionesEsPublicoSinToken() throws Exception {
        when(habitacionService.findAll()).thenReturn(List.of(crearHabitacion()));

        mockMvc.perform(get("/api/v1/habitaciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idHabitacion").value(1));

        verify(habitacionService).findAll();
    }

    @Test
    void postSinTokenNoPermiteCrearHabitacion() throws Exception {
        mockMvc.perform(post("/api/v1/habitaciones")
                        .contentType(MediaTypes.HAL_JSON)
                        .content(jsonHabitacion()))
                .andExpect(status().isForbidden());
    }

    @Test
    void userNoPuedeCrearHabitacion() throws Exception {
        configurarToken("token-user", "mavis", "USER");

        mockMvc.perform(post("/api/v1/habitaciones")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-user")
                        .contentType(MediaTypes.HAL_JSON)
                        .content(jsonHabitacion()))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminPuedeCrearHabitacion() throws Exception {
        configurarToken("token-admin", "dracula", "ADMIN");
        when(habitacionService.guardar(any(Habitacion.class))).thenReturn(crearHabitacion());

        mockMvc.perform(post("/api/v1/habitaciones")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-admin")
                        .contentType(MediaTypes.HAL_JSON)
                        .content(jsonHabitacion()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idHabitacion").value(1));

        verify(habitacionService).guardar(any(Habitacion.class));
    }

    @Test
    void tokenInvalidoRecibeUnauthorized() throws Exception {
        when(jwtService.extractNombre("token-invalido"))
                .thenThrow(new IllegalArgumentException("Token invalido"));

        mockMvc.perform(post("/api/v1/habitaciones")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-invalido")
                        .contentType(MediaTypes.HAL_JSON)
                        .content(jsonHabitacion()))
                .andExpect(status().isUnauthorized());
    }

    private void configurarToken(String token, String nombre, String rol) {
        when(jwtService.extractNombre(token)).thenReturn(nombre);
        when(jwtService.extractRol(token)).thenReturn(rol);
        when(jwtService.isTokenValid(token)).thenReturn(true);
    }

    private Habitacion crearHabitacion() {
        Habitacion habitacion = new Habitacion();
        habitacion.setIdHabitacion(1L);
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
}
