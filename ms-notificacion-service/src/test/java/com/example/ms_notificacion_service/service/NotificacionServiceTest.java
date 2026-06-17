package com.example.ms_notificacion_service.service;

import com.example.ms_notificacion_service.client.ClienteClient;
import com.example.ms_notificacion_service.client.ReservaClient;
import com.example.ms_notificacion_service.client.UsuarioClient;
import com.example.ms_notificacion_service.model.Notificacion;
import com.example.ms_notificacion_service.repository.NotificacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceTest {

    @Mock
    private NotificacionRepository notificacionRepository;

    @Mock
    private ClienteClient clienteClient;

    @Mock
    private UsuarioClient usuarioClient;

    @Mock
    private ReservaClient reservaClient;

    @InjectMocks
    private NotificacionService notificacionService;

    private Notificacion notificacion;

    @BeforeEach
    void setUp() {
        notificacion = new Notificacion();
        notificacion.setId(1L);
        notificacion.setIdCliente(1L);
        notificacion.setIdUsuario(1L);
        notificacion.setIdReserva(1L);
        notificacion.setTipo("EMAIL");
        notificacion.setMensaje("Reserva confirmada correctamente");
        notificacion.setEstado("ENVIADA");
        notificacion.setFechaEnvio(LocalDateTime.now());
    }

    @Test
    @DisplayName("Debe listar todas las notificaciones")
    void debeListarTodasLasNotificaciones() {
        // Given
        when(notificacionRepository.findAll()).thenReturn(List.of(notificacion));

        // When
        List<Notificacion> resultado = notificacionService.findAll();

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("EMAIL", resultado.get(0).getTipo());

        verify(notificacionRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe buscar una notificación por ID cuando existe")
    void debeBuscarNotificacionPorIdCuandoExiste() {
        // Given
        when(notificacionRepository.findById(1L)).thenReturn(Optional.of(notificacion));

        // When
        Notificacion resultado = notificacionService.findById(1L);

        // Then
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("EMAIL", resultado.getTipo());
        assertEquals("ENVIADA", resultado.getEstado());

        verify(notificacionRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando la notificación no existe")
    void debeLanzarExcepcionCuandoNotificacionNoExiste() {
        // Given
        when(notificacionRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> notificacionService.findById(99L)
        );

        assertEquals("Notificación no encontrada con id: 99", exception.getMessage());

        verify(notificacionRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("Debe guardar una notificación validando cliente, usuario y reserva")
    void debeGuardarNotificacionValidandoClienteUsuarioYReserva() {
        // Given
        when(notificacionRepository.save(notificacion)).thenReturn(notificacion);

        // When
        Notificacion resultado = notificacionService.guardar(notificacion);

        // Then
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("EMAIL", resultado.getTipo());
        assertEquals("Reserva confirmada correctamente", resultado.getMensaje());

        verify(clienteClient, times(1)).obtenerClientePorId(1L);
        verify(usuarioClient, times(1)).obtenerUsuarioPorId(1L);
        verify(reservaClient, times(1)).obtenerReservaPorId(1L);
        verify(notificacionRepository, times(1)).save(notificacion);
    }

    @Test
    @DisplayName("Debe actualizar una notificación existente")
    void debeActualizarNotificacionExistente() {
        // Given
        Notificacion datosActualizados = new Notificacion();
        datosActualizados.setIdCliente(2L);
        datosActualizados.setIdUsuario(3L);
        datosActualizados.setIdReserva(4L);
        datosActualizados.setTipo("SMS");
        datosActualizados.setMensaje("Reserva modificada");
        datosActualizados.setEstado("PENDIENTE");
        datosActualizados.setFechaEnvio(LocalDateTime.now());

        when(notificacionRepository.findById(1L)).thenReturn(Optional.of(notificacion));
        when(notificacionRepository.save(notificacion)).thenReturn(notificacion);

        // When
        Notificacion resultado = notificacionService.actualizar(1L, datosActualizados);

        // Then
        assertNotNull(resultado);
        assertEquals(2L, resultado.getIdCliente());
        assertEquals(3L, resultado.getIdUsuario());
        assertEquals(4L, resultado.getIdReserva());
        assertEquals("SMS", resultado.getTipo());
        assertEquals("Reserva modificada", resultado.getMensaje());
        assertEquals("PENDIENTE", resultado.getEstado());

        verify(notificacionRepository, times(1)).findById(1L);
        verify(clienteClient, times(1)).obtenerClientePorId(2L);
        verify(usuarioClient, times(1)).obtenerUsuarioPorId(3L);
        verify(reservaClient, times(1)).obtenerReservaPorId(4L);
        verify(notificacionRepository, times(1)).save(notificacion);
    }

    @Test
    @DisplayName("Debe eliminar una notificación existente")
    void debeEliminarNotificacionExistente() {
        // Given
        when(notificacionRepository.findById(1L)).thenReturn(Optional.of(notificacion));

        // When
        notificacionService.eliminar(1L);

        // Then
        verify(notificacionRepository, times(1)).findById(1L);
        verify(notificacionRepository, times(1)).delete(notificacion);
    }

    @Test
    @DisplayName("Debe buscar notificaciones por cliente")
    void debeBuscarNotificacionesPorCliente() {
        // Given
        when(notificacionRepository.findByIdCliente(1L)).thenReturn(List.of(notificacion));

        // When
        List<Notificacion> resultado = notificacionService.findByIdCliente(1L);

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());

        verify(notificacionRepository, times(1)).findByIdCliente(1L);
    }

    @Test
    @DisplayName("Debe buscar notificaciones por usuario")
    void debeBuscarNotificacionesPorUsuario() {
        // Given
        when(notificacionRepository.findByIdUsuario(1L)).thenReturn(List.of(notificacion));

        // When
        List<Notificacion> resultado = notificacionService.findByIdUsuario(1L);

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());

        verify(notificacionRepository, times(1)).findByIdUsuario(1L);
    }

    @Test
    @DisplayName("Debe buscar notificaciones por reserva")
    void debeBuscarNotificacionesPorReserva() {
        // Given
        when(notificacionRepository.findByIdReserva(1L)).thenReturn(List.of(notificacion));

        // When
        List<Notificacion> resultado = notificacionService.findByIdReserva(1L);

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());

        verify(notificacionRepository, times(1)).findByIdReserva(1L);
    }
}