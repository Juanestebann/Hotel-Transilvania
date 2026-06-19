package com.example.ms_habitacion_service.controller;

import com.example.ms_habitacion_service.model.Habitacion;
import com.example.ms_habitacion_service.security.JwtService;
import com.example.ms_habitacion_service.service.HabitacionService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    void deberiaRetornarHabitacionPorId() throws Exception {

        Habitacion habitacion = crearHabitacion();

        when(service.findById(1L))
                .thenReturn(habitacion);

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
    }

    @Test
    void deberiaCrearHabitacion() throws Exception {

        Habitacion habitacion = crearHabitacion();

        when(service.guardar(any(Habitacion.class)))
                .thenReturn(habitacion);

        String json = """
                {
                    "numeroHabitacion": "101",
                    "tipoHabitacion": "DOBLE",
                    "precioBase": 75000,
                    "capacidad": 2,
                    "estadoHabitacion": "DISPONIBLE",
                    "idHotel": 1
                }
                """;

        mockMvc.perform(post("/api/v1/habitaciones")
                        .contentType(MediaTypes.HAL_JSON)
                        .accept(MediaTypes.HAL_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idHabitacion").value(1))
                .andExpect(jsonPath("$.numeroHabitacion").value("101"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.crear-habitacion.href").exists())
                .andExpect(jsonPath("$._links.buscar-por-hotel.href").exists());

        verify(service).guardar(any(Habitacion.class));
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

    @TestConfiguration
    @EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
    static class HalTestConfig {
    }
}
