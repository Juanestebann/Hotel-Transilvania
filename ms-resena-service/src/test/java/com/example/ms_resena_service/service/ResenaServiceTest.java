package com.example.ms_resena_service.service;

import com.example.ms_resena_service.client.ReservaClient;
import com.example.ms_resena_service.dto.ReservaDTO;
import com.example.ms_resena_service.model.Resena;
import com.example.ms_resena_service.repository.ResenaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

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
    private ReservaClient reservaClient;

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
        when(reservaClient.obtenerReservaPorId(1L)).thenReturn(crearReserva(1L, 1L, 1L));

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
    @DisplayName("Debe guardar una reseña validando la reserva como fuente de verdad")
    void debeGuardarResenaValidandoReserva() {
        // Given
        when(reservaClient.obtenerReservaPorId(1L)).thenReturn(crearReserva(1L, 1L, 1L));
        when(resenaRepository.save(resena)).thenReturn(resena);

        // When
        Resena resultado = resenaService.guardarResena(resena);

        // Then
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Excelente atención", resultado.getComentario());

        verify(reservaClient).obtenerReservaPorId(1L);
        verify(resenaRepository, times(1)).save(resena);
    }

    @Test
    @DisplayName("Debe eliminar una reseña existente")
    void debeEliminarResenaExistente() {
        // Given
        when(resenaRepository.findById(1L)).thenReturn(Optional.of(resena));
        when(reservaClient.obtenerReservaPorId(1L)).thenReturn(crearReserva(1L, 1L, 1L));

        // When
        resenaService.eliminarResena(1L);

        // Then
        verify(resenaRepository, times(1)).findById(1L);
        verify(resenaRepository, times(1)).delete(resena);
    }

    @Test
    void userNoPuedeCrearResenaSobreReservaAjena() {
        when(reservaClient.obtenerReservaPorId(1L))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Reserva ajena"));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> resenaService.guardarResena(resena)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(resenaRepository, never()).save(resena);
    }

    @Test
    void noPermiteIdsQueNoCoincidenConReserva() {
        when(reservaClient.obtenerReservaPorId(1L)).thenReturn(crearReserva(2L, 1L, 1L));

        assertThrows(IllegalArgumentException.class, () -> resenaService.guardarResena(resena));

        verify(resenaRepository, never()).save(resena);
    }

    @Test
    void userNoPuedeConsultarResenaAjena() {
        when(resenaRepository.findById(1L)).thenReturn(Optional.of(resena));
        when(reservaClient.obtenerReservaPorId(1L))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Reserva ajena"));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> resenaService.buscarPorId(1L)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void userPuedeActualizarResenaPropia() {
        Resena actualizada = crearResenaActualizada();
        when(resenaRepository.findById(1L)).thenReturn(Optional.of(resena));
        when(reservaClient.obtenerReservaPorId(1L)).thenReturn(crearReserva(1L, 1L, 1L));
        when(resenaRepository.save(resena)).thenReturn(resena);

        Resena resultado = resenaService.actualizarResena(1L, actualizada);

        assertEquals("Comentario actualizado", resultado.getComentario());
        verify(resenaRepository).save(resena);
    }

    @Test
    void userNoPuedeActualizarResenaAjena() {
        when(resenaRepository.findById(1L)).thenReturn(Optional.of(resena));
        when(reservaClient.obtenerReservaPorId(1L))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Reserva ajena"));

        assertThrows(
                ResponseStatusException.class,
                () -> resenaService.actualizarResena(1L, crearResenaActualizada())
        );

        verify(resenaRepository, never()).save(any());
    }

    @Test
    void userNoPuedeEliminarResenaAjena() {
        when(resenaRepository.findById(1L)).thenReturn(Optional.of(resena));
        when(reservaClient.obtenerReservaPorId(1L))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Reserva ajena"));

        assertThrows(ResponseStatusException.class, () -> resenaService.eliminarResena(1L));

        verify(resenaRepository, never()).delete(any());
    }

    @Test
    void adminPuedeGestionarResenaDeCualquierReserva() {
        when(resenaRepository.findById(1L)).thenReturn(Optional.of(resena));
        when(reservaClient.obtenerReservaPorId(1L)).thenReturn(crearReserva(1L, 1L, 1L));

        Resena resultado = resenaService.buscarPorId(1L);

        assertEquals(1L, resultado.getId());
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

    private ReservaDTO crearReserva(Long idCliente, Long idHotel, Long idHabitacion) {
        ReservaDTO reserva = new ReservaDTO();
        reserva.setId(1L);
        reserva.setIdUsuario(1L);
        reserva.setIdCliente(idCliente);
        reserva.setIdHotel(idHotel);
        reserva.setIdHabitacion(idHabitacion);
        reserva.setEstadoReserva("FINALIZADA");
        return reserva;
    }

    private Resena crearResenaActualizada() {
        Resena actualizada = new Resena();
        actualizada.setIdCliente(1L);
        actualizada.setIdHotel(1L);
        actualizada.setIdHabitacion(1L);
        actualizada.setIdReserva(1L);
        actualizada.setCalificacion(4);
        actualizada.setComentario("Comentario actualizado");
        actualizada.setEstadoResena("APROBADA");
        return actualizada;
    }
}
