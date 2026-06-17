package com.example.ms_resena_service.service;

import com.example.ms_resena_service.client.ClienteClient;
import com.example.ms_resena_service.client.HabitacionClient;
import com.example.ms_resena_service.client.HotelClient;
import com.example.ms_resena_service.model.Resena;
import com.example.ms_resena_service.repository.ResenaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResenaServiceTest {

    @Mock
    private ResenaRepository resenaRepository;

    @Mock
    private ClienteClient clienteClient;

    @Mock
    private HotelClient hotelClient;

    @Mock
    private HabitacionClient habitacionClient;

    @InjectMocks
    private ResenaService resenaService;

    private Resena resena;

    @BeforeEach
    void setUp() {
        resena = new Resena();
        resena.setId(1L);
        resena.setIdCliente(1L);
        resena.setIdHotel(1L);
        resena.setIdHabitacion(1L);
        resena.setIdReserva(1L);
        resena.setCalificacion(5);
        resena.setComentario("Excelente atención");
        resena.setEstadoResena("APROBADA");
    }

    @Test
    @DisplayName("Debe listar todas las reseñas")
    void debeListarTodasLasResenas() {
        // Given
        when(resenaRepository.findAll()).thenReturn(List.of(resena));

        // When
        List<Resena> resultado = resenaService.listarResenas();

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Excelente atención", resultado.get(0).getComentario());

        verify(resenaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe buscar una reseña por ID cuando existe")
    void debeBuscarResenaPorIdCuandoExiste() {
        // Given
        when(resenaRepository.findById(1L)).thenReturn(Optional.of(resena));

        // When
        Resena resultado = resenaService.buscarPorId(1L);

        // Then
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(5, resultado.getCalificacion());
        assertEquals("APROBADA", resultado.getEstadoResena());

        verify(resenaRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando la reseña no existe")
    void debeLanzarExcepcionCuandoResenaNoExiste() {
        // Given
        when(resenaRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> resenaService.buscarPorId(99L)
        );

        assertEquals("No se encontró la reseña con ID: 99", exception.getMessage());

        verify(resenaRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("Debe guardar una reseña validando cliente, hotel y habitación")
    void debeGuardarResenaValidandoClienteHotelYHabitacion() {
        // Given
        when(resenaRepository.save(resena)).thenReturn(resena);

        // When
        Resena resultado = resenaService.guardarResena(resena);

        // Then
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Excelente atención", resultado.getComentario());

        verify(clienteClient, times(1)).obtenerClientePorId(1L);
        verify(hotelClient, times(1)).obtenerHotelPorId(1L);
        verify(habitacionClient, times(1)).obtenerHabitacionPorId(1L);
        verify(resenaRepository, times(1)).save(resena);
    }

    @Test
    @DisplayName("Debe eliminar una reseña existente")
    void debeEliminarResenaExistente() {
        // Given
        when(resenaRepository.findById(1L)).thenReturn(Optional.of(resena));

        // When
        resenaService.eliminarResena(1L);

        // Then
        verify(resenaRepository, times(1)).findById(1L);
        verify(resenaRepository, times(1)).delete(resena);
    }

    @Test
    @DisplayName("Debe buscar reseñas por estado en mayúsculas")
    void debeBuscarResenasPorEstadoEnMayusculas() {
        // Given
        when(resenaRepository.findByEstadoResena("APROBADA")).thenReturn(List.of(resena));

        // When
        List<Resena> resultado = resenaService.buscarPorEstado("aprobada");

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("APROBADA", resultado.get(0).getEstadoResena());

        verify(resenaRepository, times(1)).findByEstadoResena("APROBADA");
    }
}