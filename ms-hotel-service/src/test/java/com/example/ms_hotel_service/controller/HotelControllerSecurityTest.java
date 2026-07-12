package com.example.ms_hotel_service.controller;

import com.example.ms_hotel_service.model.Hotel;
import com.example.ms_hotel_service.security.JwtService;
import com.example.ms_hotel_service.service.HotelService;
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
class HotelControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HotelService hotelService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void getHotelesEsPublicoSinToken() throws Exception {
        when(hotelService.findAll()).thenReturn(List.of(crearHotel()));

        mockMvc.perform(get("/api/v1/hoteles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(hotelService).findAll();
    }

    @Test
    void postSinTokenNoPermiteCrearHotel() throws Exception {
        mockMvc.perform(post("/api/v1/hoteles")
                        .contentType(MediaTypes.HAL_JSON)
                        .content(jsonHotel()))
                .andExpect(status().isForbidden());
    }

    @Test
    void userNoPuedeCrearHotel() throws Exception {
        configurarToken("token-user", "mavis", "USER");

        mockMvc.perform(post("/api/v1/hoteles")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-user")
                        .contentType(MediaTypes.HAL_JSON)
                        .content(jsonHotel()))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminPuedeCrearHotel() throws Exception {
        configurarToken("token-admin", "dracula", "ADMIN");
        when(hotelService.guardar(any(Hotel.class))).thenReturn(crearHotel());

        mockMvc.perform(post("/api/v1/hoteles")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-admin")
                        .contentType(MediaTypes.HAL_JSON)
                        .content(jsonHotel()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(hotelService).guardar(any(Hotel.class));
    }

    @Test
    void tokenInvalidoRecibeUnauthorized() throws Exception {
        when(jwtService.extractNombre("token-invalido"))
                .thenThrow(new IllegalArgumentException("Token invalido"));

        mockMvc.perform(post("/api/v1/hoteles")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-invalido")
                        .contentType(MediaTypes.HAL_JSON)
                        .content(jsonHotel()))
                .andExpect(status().isUnauthorized());
    }

    private void configurarToken(String token, String nombre, String rol) {
        when(jwtService.extractNombre(token)).thenReturn(nombre);
        when(jwtService.extractRol(token)).thenReturn(rol);
        when(jwtService.isTokenValid(token)).thenReturn(true);
    }

    private Hotel crearHotel() {
        Hotel hotel = new Hotel();
        hotel.setId(1L);
        hotel.setNombre("Hotel Transilvania");
        hotel.setDireccion("Av Siempre Viva 123");
        hotel.setCiudad("Santiago");
        hotel.setPais("Chile");
        hotel.setCategoria("5 estrellas");
        hotel.setDescripcion("Hotel tematico");
        return hotel;
    }

    private String jsonHotel() {
        return """
                {
                    "nombre": "Hotel Transilvania",
                    "direccion": "Av Siempre Viva 123",
                    "ciudad": "Santiago",
                    "pais": "Chile",
                    "categoria": "5 estrellas",
                    "descripcion": "Hotel tematico"
                }
                """;
    }
}
