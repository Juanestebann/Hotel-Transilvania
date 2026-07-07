package com.example.ms_reserva_service.service;

import com.example.ms_reserva_service.client.ClienteClient;
import com.example.ms_reserva_service.client.DisponibilidadClient;
import com.example.ms_reserva_service.client.HabitacionClient;
import com.example.ms_reserva_service.client.HotelClient;
import com.example.ms_reserva_service.client.UsuarioClient;
import com.example.ms_reserva_service.dto.ClienteValidacionDTO;
import com.example.ms_reserva_service.dto.HabitacionDTO;
import com.example.ms_reserva_service.dto.HotelDTO;
import com.example.ms_reserva_service.dto.UsuarioDTO;
import com.example.ms_reserva_service.model.Reserva;
import com.example.ms_reserva_service.repository.ReservaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
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
    }

    @Test
    void pagoServiceNoPuedeCambiarReservaAPendiente() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.cambiarEstadoInterno(1L, "PENDIENTE")
        );
    }

    private UsuarioDTO crearUsuario(Long idUsuario, String rol) {
        UsuarioDTO usuario = new UsuarioDTO();
        usuario.setIdUsuario(idUsuario);
        usuario.setNombre("usuario-test");
        usuario.setRol(rol);
        return usuario;
    }



}
