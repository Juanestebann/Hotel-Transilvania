package com.example.ms_habitacion_service.service;

import com.example.ms_habitacion_service.client.HotelClient;
import com.example.ms_habitacion_service.dto.HotelDTO;
import com.example.ms_habitacion_service.model.Habitacion;
import com.example.ms_habitacion_service.repository.HabitacionRepository;
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
class HabitacionServiceTest {

    @Mock
    private HabitacionRepository repository;

    @Mock
    private HotelClient hotelClient;

    @InjectMocks
    private HabitacionService service;

    @Test
    void deberiaRetornarHabitacionCuandoExiste() {

        Habitacion habitacion = crearHabitacion();

        Mockito.when(repository.findById(1L))
                .thenReturn(Optional.of(habitacion));

        Habitacion resultado = service.findById(1L);

        assertEquals(1L, resultado.getIdHabitacion());
        assertEquals("101", resultado.getNumeroHabitacion());
        assertTrue(resultado.getCapacidad() >= 2);

        verify(repository).findById(1L);
    }

    @Test
    void deberiaCrearHabitacion() {

        Habitacion habitacion = crearHabitacion();
        HotelDTO hotel = new HotelDTO();
        hotel.setId(1L);

        Mockito.when(hotelClient.obtenerHotelPorId(1L))
                .thenReturn(hotel);
        Mockito.when(repository.save(habitacion))
                .thenReturn(habitacion);

        Habitacion resultado = service.guardar(habitacion);

        assertEquals(1L, resultado.getIdHabitacion());
        assertEquals("DISPONIBLE", resultado.getEstadoHabitacion());

        verify(hotelClient).obtenerHotelPorId(1L);
        verify(repository).save(habitacion);
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
}
