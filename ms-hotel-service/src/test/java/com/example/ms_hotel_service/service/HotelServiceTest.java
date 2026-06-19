package com.example.ms_hotel_service.service;

import com.example.ms_hotel_service.model.Hotel;
import com.example.ms_hotel_service.repository.HotelRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HotelServiceTest {

    @Mock
    private HotelRepository repository;

    @InjectMocks
    private HotelService service;

    @Test
    void deberiaRetornarHotelCuandoExiste() {

        Hotel hotel = crearHotel();

        Mockito.when(repository.findById(1L))
                .thenReturn(Optional.of(hotel));

        Hotel resultado = service.findById(1L);

        assertEquals(1L, resultado.getId());
        assertEquals("Hotel Transilvania", resultado.getNombre());
        assertTrue(resultado.getCategoria().contains("estrellas"));

        verify(repository).findById(1L);
    }

    @Test
    void deberiaCrearHotel() {

        Hotel hotel = crearHotel();

        Mockito.when(repository.save(hotel))
                .thenReturn(hotel);

        Hotel resultado = service.guardar(hotel);

        assertEquals("Hotel Transilvania", resultado.getNombre());
        assertEquals("Santiago", resultado.getCiudad());

        verify(repository).save(hotel);
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
}
