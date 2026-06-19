package com.example.ms_reserva_service.service;

import com.example.ms_reserva_service.client.ClienteClient;
import com.example.ms_reserva_service.client.DisponibilidadClient;
import com.example.ms_reserva_service.client.HabitacionClient;
import com.example.ms_reserva_service.client.HotelClient;
import com.example.ms_reserva_service.client.UsuarioClient;
import com.example.ms_reserva_service.dto.ClienteDTO;
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

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

        Mockito.when(clienteClient.obtenerClientePorId(1L))
                .thenReturn(new ClienteDTO());
        Mockito.when(usuarioClient.obtenerUsuarioPorId(1L))
                .thenReturn(new UsuarioDTO());
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
        verify(usuarioClient).obtenerUsuarioPorId(1L);
        verify(hotelClient).obtenerHotelPorId(1L);
        verify(habitacionClient).obtenerHabitacionPorId(1L);
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
}
