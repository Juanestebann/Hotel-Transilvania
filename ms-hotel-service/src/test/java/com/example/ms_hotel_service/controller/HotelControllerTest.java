package com.example.ms_hotel_service.controller;

import com.example.ms_hotel_service.model.Hotel;
import com.example.ms_hotel_service.security.JwtService;
import com.example.ms_hotel_service.service.HotelService;
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

@WebMvcTest(HotelController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(HotelControllerTest.HalTestConfig.class)
class HotelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HotelService service;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void deberiaRetornarHotelPorId() throws Exception {

        Hotel hotel = crearHotel();

        when(service.findById(1L))
                .thenReturn(hotel);

        mockMvc.perform(get("/api/v1/hoteles/1")
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Hotel Transilvania"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.listar-todos.href").exists())
                .andExpect(jsonPath("$._links.actualizar.href").exists())
                .andExpect(jsonPath("$._links.eliminar.href").exists());

        verify(service).findById(1L);
    }

    @Test
    void deberiaCrearHotel() throws Exception {

        Hotel hotel = crearHotel();

        when(service.guardar(any(Hotel.class)))
                .thenReturn(hotel);

        String json = """
                {
                    "nombre": "Hotel Transilvania",
                    "direccion": "Av Siempre Viva 123",
                    "ciudad": "Santiago",
                    "pais": "Chile",
                    "categoria": "5 estrellas",
                    "descripcion": "Hotel temático"
                }
                """;

        mockMvc.perform(post("/api/v1/hoteles")
                        .contentType(MediaTypes.HAL_JSON)
                        .accept(MediaTypes.HAL_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Hotel Transilvania"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.crear.href").exists())
                .andExpect(jsonPath("$._links.buscar-por-ciudad.href").exists());

        verify(service).guardar(any(Hotel.class));
    }

    @Test
    void deberiaRetornar400CuandoFaltanCamposObligatorios() throws Exception {

        String json = """
                {
                    "nombre": "Hotel Transilvania"
                }
                """;

        mockMvc.perform(post("/api/v1/hoteles")
                        .contentType(MediaTypes.HAL_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    private Hotel crearHotel() {
        Hotel hotel = new Hotel();
        hotel.setId(1L);
        hotel.setNombre("Hotel Transilvania");
        hotel.setDireccion("Av Siempre Viva 123");
        hotel.setCiudad("Santiago");
        hotel.setPais("Chile");
        hotel.setCategoria("5 estrellas");
        hotel.setDescripcion("Hotel temático");
        return hotel;
    }

    @TestConfiguration
    @EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
    static class HalTestConfig {
    }
}
