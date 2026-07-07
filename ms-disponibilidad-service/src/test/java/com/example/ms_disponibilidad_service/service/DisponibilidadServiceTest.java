package com.example.ms_disponibilidad_service.service;

import com.example.ms_disponibilidad_service.client.HabitacionClient;
import com.example.ms_disponibilidad_service.dto.HabitacionDTO;
import com.example.ms_disponibilidad_service.model.Disponibilidad;
import com.example.ms_disponibilidad_service.repository.DisponibilidadRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DisponibilidadServiceTest {

    @Mock
    private DisponibilidadRepository repository;

    @Mock
    private HabitacionClient habitacionClient;

    @InjectMocks
    private DisponibilidadService service;

    @Test
    void deberiaRetornarDisponibilidadCuandoExiste() {

        Disponibilidad disponibilidad = crearDisponibilidad();

        Mockito.when(repository.findById(1L))
                .thenReturn(Optional.of(disponibilidad));

        Disponibilidad resultado = service.findById(1L);

        assertEquals(1L, resultado.getId());
        assertEquals("DISPONIBLE", resultado.getEstado());
        assertTrue(resultado.getFecha().isEqual(LocalDate.of(2026, 6, 20)));

        verify(repository).findById(1L);
    }

    @Test
    void deberiaCrearDisponibilidad() {

        Disponibilidad disponibilidad = crearDisponibilidad();
        HabitacionDTO habitacion = new HabitacionDTO();
        habitacion.setIdHabitacion(1L);

        Mockito.when(habitacionClient.obtenerHabitacionPorId(1L))
                .thenReturn(habitacion);
        Mockito.when(repository.findByIdHabitacionAndFecha(1L, LocalDate.of(2026, 6, 20)))
                .thenReturn(Optional.empty());
        Mockito.when(repository.save(disponibilidad))
                .thenReturn(disponibilidad);

        Disponibilidad resultado = service.guardar(disponibilidad);

        assertEquals(1L, resultado.getId());
        assertEquals("DISPONIBLE", resultado.getEstado());

        verify(habitacionClient).obtenerHabitacionPorId(1L);
        verify(repository).save(disponibilidad);
    }

    @Test
    void deberiaActualizarSoloEstadoDesdeFlujoInterno() {
        Disponibilidad disponibilidad = crearDisponibilidad();
        Mockito.when(repository.findById(1L)).thenReturn(Optional.of(disponibilidad));
        Mockito.when(repository.save(disponibilidad)).thenReturn(disponibilidad);

        Disponibilidad resultado = service.actualizarEstadoInterno(1L, "OCUPADA");

        assertEquals("OCUPADA", resultado.getEstado());
        assertEquals(1L, resultado.getIdHabitacion());
        assertEquals(LocalDate.of(2026, 6, 20), resultado.getFecha());
        verify(repository).findById(1L);
        verify(repository).save(disponibilidad);
    }

    @Test
    void flujoInternoNoPuedeAsignarMantenimiento() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.actualizarEstadoInterno(1L, "MANTENIMIENTO")
        );
    }

    private Disponibilidad crearDisponibilidad() {
        Disponibilidad disponibilidad = new Disponibilidad();
        disponibilidad.setId(1L);
        disponibilidad.setIdHabitacion(1L);
        disponibilidad.setFecha(LocalDate.of(2026, 6, 20));
        disponibilidad.setEstado("DISPONIBLE");
        return disponibilidad;
    }
}
