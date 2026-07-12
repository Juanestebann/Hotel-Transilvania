package com.example.ms_hotel_service.controller;

import com.example.ms_hotel_service.model.Hotel;
import com.example.ms_hotel_service.security.JwtService;
import com.example.ms_hotel_service.service.HotelService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
    void deberiaListarHoteles() throws Exception {
        when(service.findAll()).thenReturn(List.of(crearHotel(1L), crearHotel(2L)));

        mockMvc.perform(get("/api/v1/hoteles")
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].links[0].rel").value("self"));

        verify(service).findAll();
        verifyNoMoreInteractions(service);
    }

    @Test
    void deberiaRetornarHotelPorId() throws Exception {
        when(service.findById(1L)).thenReturn(crearHotel(1L));

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
        verifyNoMoreInteractions(service);
    }

    @Test
    void buscarHotelInexistenteRetorna404() throws Exception {
        when(service.findById(99L))
                .thenThrow(new NoSuchElementException("Hotel no encontrado con id: 99"));

        mockMvc.perform(get("/api/v1/hoteles/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Hotel no encontrado con id: 99"));

        verify(service).findById(99L);
        verifyNoMoreInteractions(service);
    }

    @Test
    void deberiaFiltrarPorCategoria() throws Exception {
        when(service.findByCategoria("5 estrellas")).thenReturn(List.of(crearHotel(1L)));

        mockMvc.perform(get("/api/v1/hoteles")
                        .param("categoria", "5 estrellas")
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoria").value("5 estrellas"))
                .andExpect(jsonPath("$[0].links[5].rel").value("buscar-por-categoria"));

        verify(service).findByCategoria("5 estrellas");
        verifyNoMoreInteractions(service);
    }

    @Test
    void deberiaFiltrarPorCiudad() throws Exception {
        when(service.findByCiudad("Santiago")).thenReturn(List.of(crearHotel(1L)));

        mockMvc.perform(get("/api/v1/hoteles")
                        .param("ciudad", "Santiago")
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ciudad").value("Santiago"));

        verify(service).findByCiudad("Santiago");
        verifyNoMoreInteractions(service);
    }

    @Test
    void deberiaFiltrarPorPais() throws Exception {
        when(service.findByPais("Chile")).thenReturn(List.of(crearHotel(1L)));

        mockMvc.perform(get("/api/v1/hoteles")
                        .param("pais", "Chile")
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].pais").value("Chile"));

        verify(service).findByPais("Chile");
        verifyNoMoreInteractions(service);
    }

    @Test
    void deberiaCrearHotel() throws Exception {
        when(service.guardar(any(Hotel.class))).thenReturn(crearHotel(1L));

        mockMvc.perform(post("/api/v1/hoteles")
                        .contentType(MediaTypes.HAL_JSON)
                        .accept(MediaTypes.HAL_JSON)
                        .content(jsonHotel()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Hotel Transilvania"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.crear.href").exists())
                .andExpect(jsonPath("$._links.buscar-por-ciudad.href").exists());

        verify(service).guardar(any(Hotel.class));
        verifyNoMoreInteractions(service);
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

    @Test
    void deberiaActualizarHotel() throws Exception {
        Hotel actualizado = crearHotel(1L);
        actualizado.setNombre("Hotel Renovado");
        when(service.actualizarHotel(any(Long.class), any(Hotel.class))).thenReturn(actualizado);

        mockMvc.perform(put("/api/v1/hoteles/1")
                        .contentType(MediaTypes.HAL_JSON)
                        .accept(MediaTypes.HAL_JSON)
                        .content(jsonHotel()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Hotel Renovado"))
                .andExpect(jsonPath("$._links.actualizar.href").exists());

        verify(service).actualizarHotel(any(Long.class), any(Hotel.class));
        verifyNoMoreInteractions(service);
    }

    @Test
    void actualizarHotelInexistenteRetorna404() throws Exception {
        when(service.actualizarHotel(any(Long.class), any(Hotel.class)))
                .thenThrow(new NoSuchElementException("Hotel no encontrado con id: 99"));

        mockMvc.perform(put("/api/v1/hoteles/99")
                        .contentType(MediaTypes.HAL_JSON)
                        .content(jsonHotel()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Hotel no encontrado con id: 99"));

        verify(service).actualizarHotel(any(Long.class), any(Hotel.class));
        verifyNoMoreInteractions(service);
    }

    @Test
    void deberiaEliminarHotel() throws Exception {
        mockMvc.perform(delete("/api/v1/hoteles/1"))
                .andExpect(status().isNoContent());

        verify(service).eliminar(1L);
        verifyNoMoreInteractions(service);
    }

    @Test
    void eliminarHotelInexistenteRetorna404() throws Exception {
        doThrow(new NoSuchElementException("Hotel no encontrado con id: 99"))
                .when(service).eliminar(99L);

        mockMvc.perform(delete("/api/v1/hoteles/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Hotel no encontrado con id: 99"));

        verify(service).eliminar(99L);
        verifyNoMoreInteractions(service);
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

    private Hotel crearHotel(Long id) {
        Hotel hotel = new Hotel();
        hotel.setId(id);
        hotel.setNombre("Hotel Transilvania");
        hotel.setDireccion("Av Siempre Viva 123");
        hotel.setCiudad("Santiago");
        hotel.setPais("Chile");
        hotel.setCategoria("5 estrellas");
        hotel.setDescripcion("Hotel tematico");
        return hotel;
    }

    @TestConfiguration
    @EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
    static class HalTestConfig {
    }
}
