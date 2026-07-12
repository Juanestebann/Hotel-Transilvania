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
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class HabitacionServiceTest {

    @Mock
    private HabitacionRepository repository;

    @Mock
    private HotelClient hotelClient;

    @InjectMocks
    private HabitacionService service;

    @Test
    void deberiaListarHabitaciones() {
        Mockito.when(repository.findAll())
                .thenReturn(List.of(crearHabitacion(1L), crearHabitacion(2L)));

        List<Habitacion> resultado = service.findAll();

        assertEquals(2, resultado.size());
        assertEquals("101", resultado.get(0).getNumeroHabitacion());
        verify(repository).findAll();
        verifyNoMoreInteractions(repository, hotelClient);
    }

    @Test
    void deberiaRetornarHabitacionCuandoExiste() {
        Habitacion habitacion = crearHabitacion(1L);
        Mockito.when(repository.findById(1L)).thenReturn(Optional.of(habitacion));

        Habitacion resultado = service.findById(1L);

        assertEquals(1L, resultado.getIdHabitacion());
        assertEquals("101", resultado.getNumeroHabitacion());
        assertTrue(resultado.getCapacidad() >= 2);
        verify(repository).findById(1L);
        verifyNoMoreInteractions(repository, hotelClient);
    }

    @Test
    void deberiaLanzarErrorCuandoHabitacionNoExiste() {
        Mockito.when(repository.findById(99L)).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> service.findById(99L)
        );

        assertEquals("Habitación no encontrada con id: 99", exception.getMessage());
        verify(repository).findById(99L);
        verifyNoMoreInteractions(repository, hotelClient);
    }

    @Test
    void deberiaCrearHabitacionValidandoHotelYNormalizandoEstado() {
        Habitacion habitacion = crearHabitacion(null);
        habitacion.setEstadoHabitacion("disponible");
        Mockito.when(hotelClient.obtenerHotelPorId(1L)).thenReturn(crearHotel());
        Mockito.when(repository.save(habitacion)).thenReturn(habitacion);

        Habitacion resultado = service.guardar(habitacion);

        assertEquals("DISPONIBLE", resultado.getEstadoHabitacion());
        verify(hotelClient).obtenerHotelPorId(1L);
        verify(repository).save(habitacion);
        verifyNoMoreInteractions(repository, hotelClient);
    }

    @Test
    void crearHabitacionConHotelInexistentePropagaNotFound() {
        Habitacion habitacion = crearHabitacion(null);
        Mockito.when(hotelClient.obtenerHotelPorId(99L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Hotel no encontrado"));
        habitacion.setIdHotel(99L);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.guardar(habitacion)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(hotelClient).obtenerHotelPorId(99L);
        verify(repository, never()).save(Mockito.any());
        verifyNoMoreInteractions(repository, hotelClient);
    }

    @Test
    void crearHabitacionConEstadoInvalidoNoConsultaHotelNiGuarda() {
        Habitacion habitacion = crearHabitacion(null);
        habitacion.setEstadoHabitacion("LIMPIEZA");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.guardar(habitacion)
        );

        assertTrue(exception.getMessage().contains("Estado de habitación inválido"));
        verifyNoMoreInteractions(repository, hotelClient);
    }

    @Test
    void deberiaActualizarHabitacionExistente() {
        Habitacion existente = crearHabitacion(1L);
        Habitacion cambios = crearHabitacion(null);
        cambios.setNumeroHabitacion("202");
        cambios.setTipoHabitacion("SUITE");
        cambios.setPrecioBase(120000.0);
        cambios.setCapacidad(4);
        cambios.setEstadoHabitacion("mantenimiento");
        cambios.setIdHotel(2L);

        Mockito.when(hotelClient.obtenerHotelPorId(2L)).thenReturn(crearHotel());
        Mockito.when(repository.findById(1L)).thenReturn(Optional.of(existente));
        Mockito.when(repository.save(existente)).thenReturn(existente);

        Habitacion resultado = service.actualizar(1L, cambios);

        assertEquals("202", resultado.getNumeroHabitacion());
        assertEquals("MANTENIMIENTO", resultado.getEstadoHabitacion());
        assertEquals(2L, resultado.getIdHotel());
        verify(hotelClient).obtenerHotelPorId(2L);
        verify(repository).findById(1L);
        verify(repository).save(existente);
        verifyNoMoreInteractions(repository, hotelClient);
    }

    @Test
    void actualizarHabitacionInexistenteNoGuarda() {
        Habitacion cambios = crearHabitacion(null);
        Mockito.when(hotelClient.obtenerHotelPorId(1L)).thenReturn(crearHotel());
        Mockito.when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.actualizar(99L, cambios));

        verify(hotelClient).obtenerHotelPorId(1L);
        verify(repository).findById(99L);
        verify(repository, never()).save(Mockito.any());
        verifyNoMoreInteractions(repository, hotelClient);
    }

    @Test
    void actualizarConHotelNoDisponiblePropagaErrorRemoto() {
        Habitacion cambios = crearHabitacion(null);
        Mockito.when(hotelClient.obtenerHotelPorId(1L))
                .thenThrow(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Hotel service caido"));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.actualizar(1L, cambios)
        );

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
        verify(hotelClient).obtenerHotelPorId(1L);
        verify(repository, never()).findById(Mockito.anyLong());
        verify(repository, never()).save(Mockito.any());
        verifyNoMoreInteractions(repository, hotelClient);
    }

    @Test
    void deberiaEliminarHabitacionExistente() {
        Habitacion habitacion = crearHabitacion(1L);
        Mockito.when(repository.findById(1L)).thenReturn(Optional.of(habitacion));

        service.eliminar(1L);

        verify(repository).findById(1L);
        verify(repository).delete(habitacion);
        verifyNoMoreInteractions(repository, hotelClient);
    }

    @Test
    void eliminarHabitacionInexistenteNoInvocaDelete() {
        Mockito.when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.eliminar(99L));

        verify(repository).findById(99L);
        verify(repository, never()).delete(Mockito.any());
        verifyNoMoreInteractions(repository, hotelClient);
    }

    @Test
    void deberiaFiltrarPorEstadoNormalizado() {
        Mockito.when(repository.findByEstadoHabitacion("DISPONIBLE"))
                .thenReturn(List.of(crearHabitacion(1L)));

        List<Habitacion> resultado = service.findByEstadoHabitacion("disponible");

        assertEquals(1, resultado.size());
        verify(repository).findByEstadoHabitacion("DISPONIBLE");
        verifyNoMoreInteractions(repository, hotelClient);
    }

    @Test
    void filtrarPorEstadoInvalidoLanzaBadRequestDeDominio() {
        assertThrows(IllegalArgumentException.class, () -> service.findByEstadoHabitacion("BLOQUEADA"));

        verifyNoMoreInteractions(repository, hotelClient);
    }

    @Test
    void deberiaFiltrarPorCapacidadMinima() {
        Mockito.when(repository.findByCapacidadGreaterThanEqual(3))
                .thenReturn(List.of(crearHabitacion(1L)));

        List<Habitacion> resultado = service.findByCapacidadMinima(3);

        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).getCapacidad() >= 2);
        verify(repository).findByCapacidadGreaterThanEqual(3);
        verifyNoMoreInteractions(repository, hotelClient);
    }

    @Test
    void deberiaFiltrarPorHotel() {
        Mockito.when(repository.findByIdHotel(1L))
                .thenReturn(List.of(crearHabitacion(1L), crearHabitacion(2L)));

        List<Habitacion> resultado = service.findByIdHotel(1L);

        assertEquals(2, resultado.size());
        verify(repository).findByIdHotel(1L);
        verifyNoMoreInteractions(repository, hotelClient);
    }

    private Habitacion crearHabitacion(Long id) {
        Habitacion habitacion = new Habitacion();
        habitacion.setIdHabitacion(id);
        habitacion.setNumeroHabitacion("101");
        habitacion.setTipoHabitacion("DOBLE");
        habitacion.setPrecioBase(75000.0);
        habitacion.setCapacidad(2);
        habitacion.setEstadoHabitacion("DISPONIBLE");
        habitacion.setIdHotel(1L);
        return habitacion;
    }

    private HotelDTO crearHotel() {
        HotelDTO hotel = new HotelDTO();
        hotel.setId(1L);
        hotel.setNombre("Hotel Transilvania");
        return hotel;
    }
}
