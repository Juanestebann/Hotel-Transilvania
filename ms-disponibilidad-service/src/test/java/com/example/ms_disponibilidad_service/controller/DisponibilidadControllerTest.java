package com.example.ms_disponibilidad_service.controller;

import com.example.ms_disponibilidad_service.model.Disponibilidad;
import com.example.ms_disponibilidad_service.security.JwtService;
import com.example.ms_disponibilidad_service.service.DisponibilidadService;
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

@WebMvcTest(DisponibilidadController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(DisponibilidadControllerTest.HalTestConfig.class)
class DisponibilidadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DisponibilidadService service;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void deberiaRetornarDisponibilidadPorId() throws Exception {

        Disponibilidad disponibilidad = crearDisponibilidad();

        when(service.findById(1L))
                .thenReturn(disponibilidad);

        mockMvc.perform(get("/api/v1/disponibilidades/1")
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.estado").value("DISPONIBLE"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.listar-todos.href").exists())
                .andExpect(jsonPath("$._links.actualizar.href").exists())
                .andExpect(jsonPath("$._links.eliminar.href").exists());

        verify(service).findById(1L);
    }

    @Test
    void deberiaCrearDisponibilidad() throws Exception {

        Disponibilidad disponibilidad = crearDisponibilidad();

        when(service.guardar(any(Disponibilidad.class)))
                .thenReturn(disponibilidad);

        String json = """
                {
                    "idHabitacion": 1,
                    "fecha": "2026-06-20",
                    "estado": "DISPONIBLE"
                }
                """;

        mockMvc.perform(post("/api/v1/disponibilidades")
                        .contentType(MediaTypes.HAL_JSON)
                        .accept(MediaTypes.HAL_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.idHabitacion").value(1))
                .andExpect(jsonPath("$.estado").value("DISPONIBLE"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.crear.href").exists())
                .andExpect(jsonPath("$._links.buscar-por-habitacion-fecha.href").exists());

        verify(service).guardar(any(Disponibilidad.class));
    }

    @Test
    void deberiaRetornar400CuandoFaltanCamposObligatorios() throws Exception {

        String json = """
                {
                    "estado": "DISPONIBLE"
                }
                """;

        mockMvc.perform(post("/api/v1/disponibilidades")
                        .contentType(MediaTypes.HAL_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    private Disponibilidad crearDisponibilidad() {
        Disponibilidad disponibilidad = new Disponibilidad();
        disponibilidad.setId(1L);
        disponibilidad.setIdHabitacion(1L);
        disponibilidad.setFecha(LocalDate.of(2026, 6, 20));
        disponibilidad.setEstado("DISPONIBLE");
        return disponibilidad;
    }

    @TestConfiguration
    @EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
    static class HalTestConfig {
    }
}
