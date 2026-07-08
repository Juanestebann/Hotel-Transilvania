package com.example.ms_reserva_service.service;

import com.example.ms_reserva_service.client.ClienteClient;
import com.example.ms_reserva_service.client.DisponibilidadClient;
import com.example.ms_reserva_service.client.HabitacionClient;
import com.example.ms_reserva_service.client.HotelClient;
import com.example.ms_reserva_service.client.UsuarioClient;
import com.example.ms_reserva_service.dto.ClienteValidacionDTO;
import com.example.ms_reserva_service.dto.DisponibilidadDTO;
import com.example.ms_reserva_service.dto.HabitacionDTO;
import com.example.ms_reserva_service.dto.HotelDTO;
import com.example.ms_reserva_service.dto.UsuarioDTO;
import com.example.ms_reserva_service.model.Reserva;
import com.example.ms_reserva_service.repository.ReservaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock
    private ReservaRepository repository;

    @Mock
    private ClienteClient clienteClient;

    @Mock
    private UsuarioClient usuarioClient;

    @Mock
    private HabitacionClient habitacionClient;

    @Mock
    private HotelClient hotelClient;

    @Mock
    private DisponibilidadClient disponibilidadClient;

    @InjectMocks
    private ReservaService service;

    @Test
    void deberiaRetornarReservaCuandoExiste() {

        Reserva reserva = crearReserva();

        Mockito.when(usuarioClient.obtenerUsuarioActual())
                .thenReturn(crearUsuario(1L, "USER"));
        Mockito.when(repository.findById(1L))
                .thenReturn(Optional.of(reserva));

        Reserva resultado = service.findById(1L);

        assertEquals(1L, resultado.getId());
        assertEquals("PENDIENTE", resultado.getEstadoReserva());
        assertTrue(resultado.getFechaFin().isAfter(resultado.getFechaInicio()));

        verify(repository).findById(1L);
    }

    @Test
    void deberiaCrearReserva() {

        Reserva reserva = crearReserva();

        Mockito.when(usuarioClient.obtenerUsuarioActual())
                .thenReturn(crearUsuario(1L, "USER"));
        Mockito.when(clienteClient.obtenerClientePorId(1L))
                .thenReturn(new ClienteValidacionDTO(1L, true));
        Mockito.when(hotelClient.obtenerHotelPorId(1L))
                .thenReturn(new HotelDTO());
        Mockito.when(habitacionClient.obtenerHabitacionPorId(1L))
                .thenReturn(new HabitacionDTO(1L, "101", "DOBLE", 75000.0, 2, "DISPONIBLE", 1L));
        Mockito.when(repository.save(reserva))
                .thenReturn(reserva);

        Reserva resultado = service.guardar(reserva);

        assertEquals(1L, resultado.getId());
        assertEquals("PENDIENTE", resultado.getEstadoReserva());

        verify(clienteClient).obtenerClientePorId(1L);
        verify(usuarioClient).obtenerUsuarioActual();
        verify(usuarioClient, never()).obtenerUsuarioPorId(anyLong());
        verify(hotelClient).obtenerHotelPorId(1L);
        verify(habitacionClient).obtenerHabitacionPorId(1L);
        verify(repository).save(reserva);
    }

    @Test
    void adminPuedeCrearReservaParaUsuarioIndicado() {
        Reserva reserva = crearReserva();
        reserva.setIdUsuario(25L);

        Mockito.when(usuarioClient.obtenerUsuarioActual())
                .thenReturn(crearUsuario(1L, "ADMIN"));
        Mockito.when(clienteClient.obtenerClientePorId(1L))
                .thenReturn(new ClienteValidacionDTO(1L, true));
        Mockito.when(usuarioClient.obtenerUsuarioPorId(25L))
                .thenReturn(crearUsuario(25L, "USER"));
        Mockito.when(hotelClient.obtenerHotelPorId(1L))
                .thenReturn(new HotelDTO());
        Mockito.when(habitacionClient.obtenerHabitacionPorId(1L))
                .thenReturn(new HabitacionDTO(1L, "101", "DOBLE", 75000.0, 2, "DISPONIBLE", 1L));
        Mockito.when(repository.save(reserva)).thenReturn(reserva);

        Reserva resultado = service.guardar(reserva);

        assertEquals(25L, resultado.getIdUsuario());
        verify(usuarioClient).obtenerUsuarioPorId(25L);
        verify(repository).save(reserva);
    }

    private Reserva crearReserva() {
        Reserva reserva = new Reserva();
        reserva.setId(1L);
        reserva.setIdCliente(1L);
        reserva.setIdUsuario(1L);
        reserva.setIdHotel(1L);
        reserva.setIdHabitacion(1L);
        reserva.setFechaInicio(LocalDate.of(2026, 6, 20));
        reserva.setFechaFin(LocalDate.of(2026, 6, 22));
        reserva.setCantidadPersonas(2);
        reserva.setEstadoReserva("PENDIENTE");
        return reserva;
    }

    @Test
    void deberiaLanzarErrorCuandoReservaNoExiste(){
        //Given

        Mockito.when(usuarioClient.obtenerUsuarioActual())
                .thenReturn(crearUsuario(1L, "ADMIN"));
        Mockito.when(repository.findById(99L)).thenReturn(Optional.empty());

        //When / Then

        RuntimeException exception = assertThrows(
                RuntimeException.class,() -> service.findById(99L)
        );

        verify(repository).findById(99L);
    }

    @Test
    void userNoPuedeCrearReservaParaOtroUsuario() {
        Reserva reserva = crearReserva();
        reserva.setIdUsuario(2L);

        Mockito.when(usuarioClient.obtenerUsuarioActual())
                .thenReturn(crearUsuario(1L, "USER"));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.guardar(reserva)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(repository, never()).save(reserva);
    }

    @Test
    void userNoPuedeCrearReservaConfirmada() {
        Reserva reserva = crearReserva();
        reserva.setEstadoReserva("CONFIRMADA");

        Mockito.when(usuarioClient.obtenerUsuarioActual())
                .thenReturn(crearUsuario(1L, "USER"));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.guardar(reserva)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(repository, never()).save(reserva);
    }

    @Test
    void userPuedeConsultarReservaPropia() {
        Reserva reserva = crearReserva();

        Mockito.when(usuarioClient.obtenerUsuarioActual())
                .thenReturn(crearUsuario(1L, "USER"));
        Mockito.when(repository.findById(1L)).thenReturn(Optional.of(reserva));

        Reserva resultado = service.findById(1L);

        assertEquals(1L, resultado.getIdUsuario());
    }

    @Test
    void userNoPuedeConsultarReservaAjena() {
        Reserva reserva = crearReserva();
        reserva.setIdUsuario(2L);

        Mockito.when(usuarioClient.obtenerUsuarioActual())
                .thenReturn(crearUsuario(1L, "USER"));
        Mockito.when(repository.findById(1L)).thenReturn(Optional.of(reserva));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.findById(1L)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void adminPuedeConsultarCualquierReserva() {
        Reserva reserva = crearReserva();
        reserva.setIdUsuario(25L);

        Mockito.when(usuarioClient.obtenerUsuarioActual())
                .thenReturn(crearUsuario(1L, "ADMIN"));
        Mockito.when(repository.findById(1L)).thenReturn(Optional.of(reserva));

        Reserva resultado = service.findById(1L);

        assertEquals(25L, resultado.getIdUsuario());
    }

    @Test
    void userNoPuedeConfirmarReservaDirectamente() {
        Reserva reserva = crearReserva();

        Mockito.when(usuarioClient.obtenerUsuarioActual())
                .thenReturn(crearUsuario(1L, "USER"));
        Mockito.when(repository.findById(1L)).thenReturn(Optional.of(reserva));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.cambiarEstado(1L, "CONFIRMADA")
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(repository, never()).save(reserva);
    }

    @Test
    void userPuedeCancelarReservaPropia() {
        Reserva reserva = crearReserva();

        Mockito.when(usuarioClient.obtenerUsuarioActual())
                .thenReturn(crearUsuario(1L, "USER"));
        Mockito.when(repository.findById(1L)).thenReturn(Optional.of(reserva));
        Mockito.when(repository.save(reserva)).thenReturn(reserva);

        Reserva resultado = service.cambiarEstado(1L, "CANCELADA");

        assertEquals("CANCELADA", resultado.getEstadoReserva());
        verify(repository).save(reserva);
    }

    @Test
    void pagoServicePuedeConfirmarYActualizarDisponibilidadSinIdentidadUser() {
        Reserva reserva = crearReserva();
        Mockito.when(repository.findById(1L)).thenReturn(Optional.of(reserva));
        Mockito.when(disponibilidadClient.obtenerDisponibilidadPorHabitacionYFecha(
                        Mockito.eq(1L), Mockito.any(LocalDate.class)))
                .thenAnswer(invocation -> new com.example.ms_reserva_service.dto.DisponibilidadDTO(
                        10L,
                        1L,
                        invocation.getArgument(1),
                        "DISPONIBLE"
                ));
        Mockito.when(repository.save(reserva)).thenReturn(reserva);

        Reserva resultado = service.cambiarEstadoInterno(1L, "CONFIRMADA");

        assertEquals("CONFIRMADA", resultado.getEstadoReserva());
        Mockito.verify(usuarioClient, Mockito.never()).obtenerUsuarioActual();
        Mockito.verify(disponibilidadClient, Mockito.times(2))
                .actualizarDisponibilidad(Mockito.eq(10L), Mockito.any());
        InOrder orden = Mockito.inOrder(disponibilidadClient, repository);
        orden.verify(disponibilidadClient, Mockito.times(2))
                .actualizarDisponibilidad(Mockito.eq(10L), Mockito.any());
        orden.verify(repository).save(reserva);
    }

    @Test
    void pagoServiceNoPuedeCambiarReservaAPendiente() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.cambiarEstadoInterno(1L, "PENDIENTE")
        );
    }

    @Test
    void siDisponibilidadFallaReservaNoQuedaConfirmada() {
        Reserva reserva = crearReserva();
        DisponibilidadDTO disponibilidad = crearDisponibilidad(10L, reserva.getFechaInicio(), "DISPONIBLE");
        Mockito.when(repository.findById(1L)).thenReturn(Optional.of(reserva));
        Mockito.when(disponibilidadClient.obtenerDisponibilidadPorHabitacionYFecha(
                        1L, reserva.getFechaInicio()))
                .thenReturn(disponibilidad);
        Mockito.when(disponibilidadClient.obtenerDisponibilidadPorHabitacionYFecha(
                        1L, reserva.getFechaInicio().plusDays(1)))
                .thenReturn(crearDisponibilidad(11L, reserva.getFechaInicio().plusDays(1), "DISPONIBLE"));
        Mockito.when(disponibilidadClient.actualizarDisponibilidad(
                        Mockito.eq(10L), Mockito.any()))
                .thenThrow(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE))
                .thenReturn(disponibilidad);

        assertThrows(
                ResponseStatusException.class,
                () -> service.cambiarEstadoInterno(1L, "CONFIRMADA")
        );

        assertEquals("PENDIENTE", reserva.getEstadoReserva());
        Mockito.verify(repository, never()).save(reserva);
    }

    @Test
    void siFallaSegundaFechaCompensaLaPrimera() {
        Reserva reserva = crearReserva();
        DisponibilidadDTO primera = crearDisponibilidad(10L, reserva.getFechaInicio(), "DISPONIBLE");
        DisponibilidadDTO segunda = crearDisponibilidad(11L, reserva.getFechaInicio().plusDays(1), "DISPONIBLE");
        List<String> operaciones = new ArrayList<>();

        Mockito.when(repository.findById(1L)).thenReturn(Optional.of(reserva));
        Mockito.when(disponibilidadClient.obtenerDisponibilidadPorHabitacionYFecha(
                        1L, reserva.getFechaInicio()))
                .thenReturn(primera);
        Mockito.when(disponibilidadClient.obtenerDisponibilidadPorHabitacionYFecha(
                        1L, reserva.getFechaInicio().plusDays(1)))
                .thenReturn(segunda);
        Mockito.when(disponibilidadClient.actualizarDisponibilidad(
                        Mockito.anyLong(), Mockito.any()))
                .thenAnswer(invocation -> {
                    Long id = invocation.getArgument(0);
                    DisponibilidadDTO dto = invocation.getArgument(1);
                    operaciones.add(id + ":" + dto.getEstado());
                    if (id.equals(11L) && "OCUPADA".equals(dto.getEstado())) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
                    }
                    return dto;
                });

        assertThrows(
                ResponseStatusException.class,
                () -> service.cambiarEstadoInterno(1L, "CONFIRMADA")
        );

        assertEquals(List.of("10:OCUPADA", "11:OCUPADA", "10:DISPONIBLE"), operaciones);
        assertEquals("PENDIENTE", reserva.getEstadoReserva());
        Mockito.verify(repository, never()).save(reserva);
    }

    @Test
    void falloDeCompensacionConTimeoutSeReportaComoGatewayTimeout() {
        Reserva reserva = crearReserva();
        DisponibilidadDTO primera = crearDisponibilidad(10L, reserva.getFechaInicio(), "DISPONIBLE");
        DisponibilidadDTO segunda = crearDisponibilidad(11L, reserva.getFechaInicio().plusDays(1), "DISPONIBLE");

        Mockito.when(repository.findById(1L)).thenReturn(Optional.of(reserva));
        Mockito.when(disponibilidadClient.obtenerDisponibilidadPorHabitacionYFecha(
                        1L, reserva.getFechaInicio()))
                .thenReturn(primera);
        Mockito.when(disponibilidadClient.obtenerDisponibilidadPorHabitacionYFecha(
                        1L, reserva.getFechaInicio().plusDays(1)))
                .thenReturn(segunda);
        Mockito.when(disponibilidadClient.actualizarDisponibilidad(
                        Mockito.anyLong(), Mockito.any()))
                .thenAnswer(invocation -> {
                    Long id = invocation.getArgument(0);
                    DisponibilidadDTO dto = invocation.getArgument(1);
                    if (id.equals(11L) && "OCUPADA".equals(dto.getEstado())) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
                    }
                    if (id.equals(10L) && "DISPONIBLE".equals(dto.getEstado())) {
                        throw new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT);
                    }
                    return dto;
                });

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.cambiarEstadoInterno(1L, "CONFIRMADA")
        );

        assertEquals(HttpStatus.GATEWAY_TIMEOUT, exception.getStatusCode());
        assertTrue(exception.getReason().contains("compensación"));
        assertEquals("PENDIENTE", reserva.getEstadoReserva());
        Mockito.verify(repository, never()).save(reserva);
    }

    @Test
    void cancelarReservaConfirmadaLiberaAntesDeGuardar() {
        Reserva reserva = crearReserva();
        reserva.setEstadoReserva("CONFIRMADA");
        Mockito.when(usuarioClient.obtenerUsuarioActual()).thenReturn(crearUsuario(1L, "USER"));
        Mockito.when(repository.findById(1L)).thenReturn(Optional.of(reserva));
        Mockito.when(disponibilidadClient.obtenerDisponibilidadPorHabitacionYFecha(
                        Mockito.eq(1L), Mockito.any(LocalDate.class)))
                .thenAnswer(invocation -> crearDisponibilidad(
                        10L,
                        invocation.getArgument(1),
                        "OCUPADA"
                ));
        Mockito.when(repository.save(reserva)).thenReturn(reserva);

        Reserva resultado = service.cambiarEstado(1L, "CANCELADA");

        assertEquals("CANCELADA", resultado.getEstadoReserva());
        InOrder orden = Mockito.inOrder(disponibilidadClient, repository);
        orden.verify(disponibilidadClient, Mockito.times(2))
                .actualizarDisponibilidad(Mockito.eq(10L), Mockito.any());
        orden.verify(repository).save(reserva);
    }

    @Test
    void siLiberacionFallaReservaPermaneceConfirmada() {
        Reserva reserva = crearReserva();
        reserva.setEstadoReserva("CONFIRMADA");
        DisponibilidadDTO disponibilidad = crearDisponibilidad(10L, reserva.getFechaInicio(), "OCUPADA");
        Mockito.when(usuarioClient.obtenerUsuarioActual()).thenReturn(crearUsuario(1L, "USER"));
        Mockito.when(repository.findById(1L)).thenReturn(Optional.of(reserva));
        Mockito.when(disponibilidadClient.obtenerDisponibilidadPorHabitacionYFecha(
                        1L, reserva.getFechaInicio()))
                .thenReturn(disponibilidad);
        Mockito.when(disponibilidadClient.actualizarDisponibilidad(
                        Mockito.eq(10L), Mockito.any()))
                .thenThrow(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE))
                .thenReturn(disponibilidad);

        assertThrows(
                ResponseStatusException.class,
                () -> service.cambiarEstado(1L, "CANCELADA")
        );

        assertEquals("CONFIRMADA", reserva.getEstadoReserva());
        Mockito.verify(repository, never()).save(reserva);
    }

    @Test
    void estadoTerminalNoPuedeVolverAConfirmada() {
        Reserva reserva = crearReserva();
        reserva.setEstadoReserva("CANCELADA");
        Mockito.when(repository.findById(1L)).thenReturn(Optional.of(reserva));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.cambiarEstadoInterno(1L, "CONFIRMADA")
        );

        Mockito.verifyNoInteractions(disponibilidadClient);
        Mockito.verify(repository, never()).save(reserva);
    }

    @Test
    void repetirMismoEstadoEsIdempotente() {
        Reserva reserva = crearReserva();
        reserva.setEstadoReserva("CONFIRMADA");
        Mockito.when(repository.findById(1L)).thenReturn(Optional.of(reserva));

        Reserva resultado = service.cambiarEstadoInterno(1L, "CONFIRMADA");

        assertEquals("CONFIRMADA", resultado.getEstadoReserva());
        Mockito.verifyNoInteractions(disponibilidadClient);
        Mockito.verify(repository, never()).save(reserva);
    }

    private DisponibilidadDTO crearDisponibilidad(
            Long id,
            LocalDate fecha,
            String estado
    ) {
        return new DisponibilidadDTO(id, 1L, fecha, estado);
    }

    private UsuarioDTO crearUsuario(Long idUsuario, String rol) {
        UsuarioDTO usuario = new UsuarioDTO();
        usuario.setIdUsuario(idUsuario);
        usuario.setNombre("usuario-test");
        usuario.setRol(rol);
        return usuario;
    }



}
