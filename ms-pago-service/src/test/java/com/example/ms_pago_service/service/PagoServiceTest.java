package com.example.ms_pago_service.service;

import com.example.ms_pago_service.client.ReservaClient;
import com.example.ms_pago_service.client.UsuarioClient;
import com.example.ms_pago_service.dto.ReservaDTO;
import com.example.ms_pago_service.dto.UsuarioDTO;
import com.example.ms_pago_service.model.Pago;
import com.example.ms_pago_service.repository.PagoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PagoServiceTest {

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private ReservaClient reservaClient;

    @Mock
    private UsuarioClient usuarioClient;

    @InjectMocks
    private PagoService pagoService;

    @Test
    void deberiaListarPagos() {
        Mockito.when(pagoRepository.findAll()).thenReturn(List.of(crearPago("APROBADO")));

        List<Pago> resultado = pagoService.findAll();

        assertEquals(1, resultado.size());
        verify(pagoRepository).findAll();
    }

    @Test
    void userPuedeConsultarPagoPropioPorId() {
        Pago pago = crearPago("APROBADO");

        Mockito.when(usuarioClient.obtenerUsuarioActual())
                .thenReturn(crearUsuario(1L, "USER"));
        Mockito.when(pagoRepository.findById(1L)).thenReturn(Optional.of(pago));

        Pago resultado = pagoService.findById(1L);

        assertEquals(1L, resultado.getIdPago());
        assertEquals("APROBADO", resultado.getEstadoPago());
        verify(pagoRepository).findById(1L);
    }

    @Test
    void userNoPuedeConsultarPagoAjenoPorId() {
        Pago pagoAjeno = crearPago("APROBADO");
        pagoAjeno.setIdUsuario(2L);

        Mockito.when(usuarioClient.obtenerUsuarioActual())
                .thenReturn(crearUsuario(1L, "USER"));
        Mockito.when(pagoRepository.findById(1L)).thenReturn(Optional.of(pagoAjeno));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> pagoService.findById(1L)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void adminPuedeConsultarCualquierPagoPorId() {
        Pago pagoAjeno = crearPago("APROBADO");
        pagoAjeno.setIdUsuario(25L);

        Mockito.when(usuarioClient.obtenerUsuarioActual())
                .thenReturn(crearUsuario(1L, "ADMIN"));
        Mockito.when(pagoRepository.findById(1L)).thenReturn(Optional.of(pagoAjeno));

        Pago resultado = pagoService.findById(1L);

        assertEquals(25L, resultado.getIdUsuario());
    }

    @Test
    void deberiaLanzarErrorCuandoPagoNoExiste() {
        Mockito.when(usuarioClient.obtenerUsuarioActual())
                .thenReturn(crearUsuario(1L, "USER"));
        Mockito.when(pagoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> pagoService.findById(99L));

        verify(pagoRepository).findById(99L);
    }

    @Test
    void deberiaCrearPagoAprobadoYCambiarReservaAConfirmada() {
        Pago pago = crearPago("APROBADO");

        prepararFlujoUserPropio();
        Mockito.when(pagoRepository.save(pago)).thenReturn(pago);

        Pago resultado = pagoService.save(pago);

        assertEquals("APROBADO", resultado.getEstadoPago());
        verify(reservaClient).obtenerReservaPorId(1L);
        verify(usuarioClient).obtenerUsuarioActual();
        Mockito.verify(usuarioClient, Mockito.never()).obtenerUsuarioPorId(Mockito.anyLong());
        verify(pagoRepository).save(pago);
        verify(reservaClient).cambiarEstadoReserva(1L, "CONFIRMADA");
    }

    @Test
    void deberiaCambiarReservaACanceladaCuandoPagoEsRechazado() {
        Pago pago = crearPago("RECHAZADO");

        prepararFlujoUserPropio();
        Mockito.when(pagoRepository.save(pago)).thenReturn(pago);

        pagoService.save(pago);

        verify(reservaClient).cambiarEstadoReserva(1L, "CANCELADA");
    }

    @Test
    void deberiaCambiarReservaACanceladaCuandoPagoEsReembolsado() {
        Pago pago = crearPago("REEMBOLSADO");

        prepararFlujoUserPropio();
        Mockito.when(pagoRepository.save(pago)).thenReturn(pago);

        pagoService.save(pago);

        verify(reservaClient).cambiarEstadoReserva(1L, "CANCELADA");
    }

    @Test
    void pagoPendienteNoDebeCambiarEstadoDeReserva() {
        Pago pago = crearPago("PENDIENTE");

        prepararFlujoUserPropio();
        Mockito.when(pagoRepository.save(pago)).thenReturn(pago);

        pagoService.save(pago);

        Mockito.verify(reservaClient, Mockito.never())
                .cambiarEstadoReserva(Mockito.anyLong(), Mockito.anyString());
    }

    @Test
    void noDeberiaCambiarReservaCuandoPagoEsAnulado() {
        Pago pago = crearPago("ANULADO");

        prepararFlujoUserPropio();
        Mockito.when(pagoRepository.save(pago)).thenReturn(pago);

        pagoService.save(pago);

        Mockito.verify(reservaClient, Mockito.never())
                .cambiarEstadoReserva(Mockito.anyLong(), Mockito.anyString());
    }

    @Test
    void deberiaActualizarPago() {
        Pago existente = crearPago("PENDIENTE");
        Pago actualizado = crearPago("APROBADO");
        actualizado.setMonto(new BigDecimal("80000"));

        Mockito.when(pagoRepository.findById(1L)).thenReturn(Optional.of(existente));
        Mockito.when(pagoRepository.save(existente)).thenReturn(existente);

        Pago resultado = pagoService.update(1L, actualizado);

        assertEquals(new BigDecimal("80000"), resultado.getMonto());
        assertEquals("APROBADO", resultado.getEstadoPago());

        verify(pagoRepository).findById(1L);
        verify(reservaClient).obtenerReservaPorId(1L);
        verify(usuarioClient).obtenerUsuarioPorId(1L);
        verify(pagoRepository).save(existente);
        verify(reservaClient).cambiarEstadoReserva(1L, "CONFIRMADA");
    }

    @Test
    void deberiaEliminarPago() {
        Pago pago = crearPago("APROBADO");

        Mockito.when(pagoRepository.findById(1L)).thenReturn(Optional.of(pago));

        pagoService.delete(1L);

        verify(pagoRepository).findById(1L);
        verify(pagoRepository).delete(pago);
    }

    @Test
    void userPuedeConsultarPagosDeReservaPropia() {
        Mockito.when(usuarioClient.obtenerUsuarioActual())
                .thenReturn(crearUsuario(1L, "USER"));
        Mockito.when(reservaClient.obtenerReservaPorId(1L))
                .thenReturn(crearReserva(1L));
        Mockito.when(pagoRepository.findByReservaId(1L))
                .thenReturn(List.of(crearPago("APROBADO")));

        List<Pago> resultado = pagoService.findByReservaId(1L);

        assertEquals(1, resultado.size());
        verify(reservaClient).obtenerReservaPorId(1L);
        verify(pagoRepository).findByReservaId(1L);
    }

    @Test
    void userNoPuedeConsultarPagosDeReservaAjena() {
        Mockito.when(usuarioClient.obtenerUsuarioActual())
                .thenReturn(crearUsuario(1L, "USER"));
        Mockito.when(reservaClient.obtenerReservaPorId(1L))
                .thenReturn(crearReserva(2L));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> pagoService.findByReservaId(1L)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        Mockito.verify(pagoRepository, Mockito.never()).findByReservaId(1L);
    }

    @Test
    void userNoPuedeRecibirPagosAjenosDentroDeReservaPropia() {
        Pago pagoAjeno = crearPago("APROBADO");
        pagoAjeno.setIdUsuario(2L);

        Mockito.when(usuarioClient.obtenerUsuarioActual())
                .thenReturn(crearUsuario(1L, "USER"));
        Mockito.when(reservaClient.obtenerReservaPorId(1L))
                .thenReturn(crearReserva(1L));
        Mockito.when(pagoRepository.findByReservaId(1L))
                .thenReturn(List.of(pagoAjeno));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> pagoService.findByReservaId(1L)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void adminPuedeConsultarPagosDeCualquierReserva() {
        Pago pagoAjeno = crearPago("APROBADO");
        pagoAjeno.setIdUsuario(25L);

        Mockito.when(usuarioClient.obtenerUsuarioActual())
                .thenReturn(crearUsuario(1L, "ADMIN"));
        Mockito.when(pagoRepository.findByReservaId(1L))
                .thenReturn(List.of(pagoAjeno));

        List<Pago> resultado = pagoService.findByReservaId(1L);

        assertEquals(25L, resultado.get(0).getIdUsuario());
        Mockito.verify(reservaClient, Mockito.never()).obtenerReservaPorId(1L);
    }

    @Test
    void deberiaBuscarPagosPorEstadoPago() {
        Mockito.when(pagoRepository.findByEstadoPago("APROBADO")).thenReturn(List.of(crearPago("APROBADO")));

        List<Pago> resultado = pagoService.findByEstadoPago("aprobado");

        assertEquals(1, resultado.size());
        verify(pagoRepository).findByEstadoPago("APROBADO");
    }

    @Test
    void deberiaLanzarErrorCuandoMontoEsCero() {
        Pago pago = crearPago("APROBADO");
        pago.setMonto(BigDecimal.ZERO);

        assertThrows(IllegalArgumentException.class, () -> pagoService.save(pago));
    }

    @Test
    void deberiaLanzarErrorCuandoMontoEsNull() {
        Pago pago = crearPago("APROBADO");
        pago.setMonto(null);

        assertThrows(IllegalArgumentException.class, () -> pagoService.save(pago));
    }

    @Test
    void deberiaLanzarErrorCuandoEstadoPagoEsInvalido() {
        Pago pago = crearPago("INVALIDO");

        assertThrows(IllegalArgumentException.class, () -> pagoService.save(pago));
    }

    @Test
    void deberiaLanzarErrorCuandoEstadoPagoEsVacio() {
        Pago pago = crearPago("");

        assertThrows(IllegalArgumentException.class, () -> pagoService.save(pago));
    }

    @Test
    void userNoPuedeCrearPagoConIdUsuarioAjeno() {
        Pago pago = crearPago("APROBADO");
        pago.setIdUsuario(2L);
        Mockito.when(usuarioClient.obtenerUsuarioActual())
                .thenReturn(crearUsuario(1L, "USER"));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> pagoService.save(pago)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        Mockito.verify(pagoRepository, Mockito.never()).save(pago);
    }

    @Test
    void userNoPuedePagarReservaAjena() {
        Pago pago = crearPago("APROBADO");
        Mockito.when(usuarioClient.obtenerUsuarioActual())
                .thenReturn(crearUsuario(1L, "USER"));
        Mockito.when(reservaClient.obtenerReservaPorId(1L))
                .thenReturn(crearReserva(2L));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> pagoService.save(pago)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        Mockito.verify(pagoRepository, Mockito.never()).save(pago);
    }

    @Test
    void adminPuedeCrearPagoParaUsuarioIndicado() {
        Pago pago = crearPago("APROBADO");
        pago.setIdUsuario(25L);
        Mockito.when(usuarioClient.obtenerUsuarioActual())
                .thenReturn(crearUsuario(1L, "ADMIN"));
        Mockito.when(reservaClient.obtenerReservaPorId(1L))
                .thenReturn(crearReserva(25L));
        Mockito.when(usuarioClient.obtenerUsuarioPorId(25L))
                .thenReturn(crearUsuario(25L, "USER"));
        Mockito.when(pagoRepository.save(pago)).thenReturn(pago);

        Pago resultado = pagoService.save(pago);

        assertEquals(25L, resultado.getIdUsuario());
        verify(usuarioClient).obtenerUsuarioPorId(25L);
        verify(reservaClient).cambiarEstadoReserva(1L, "CONFIRMADA");
    }

    @Test
    void noPermiteSegundoPagoCuandoReservaYaTienePagoAprobado() {
        Pago pago = crearPago("APROBADO");
        prepararFlujoUserPropio();
        Mockito.when(pagoRepository.findByReservaId(1L))
                .thenReturn(List.of(crearPago("APROBADO")));

        assertThrows(IllegalArgumentException.class, () -> pagoService.save(pago));

        Mockito.verify(pagoRepository, Mockito.never()).save(pago);
    }

    private void prepararFlujoUserPropio() {
        Mockito.when(usuarioClient.obtenerUsuarioActual())
                .thenReturn(crearUsuario(1L, "USER"));
        Mockito.when(reservaClient.obtenerReservaPorId(1L))
                .thenReturn(crearReserva(1L));
    }

    private UsuarioDTO crearUsuario(Long idUsuario, String rol) {
        UsuarioDTO usuario = new UsuarioDTO();
        usuario.setIdUsuario(idUsuario);
        usuario.setNombre("usuario-test");
        usuario.setRol(rol);
        return usuario;
    }

    private ReservaDTO crearReserva(Long idUsuario) {
        ReservaDTO reserva = new ReservaDTO();
        reserva.setId(1L);
        reserva.setIdUsuario(idUsuario);
        reserva.setEstadoReserva("PENDIENTE");
        return reserva;
    }

    private Pago crearPago(String estadoPago) {
        Pago pago = new Pago();
        pago.setIdPago(1L);
        pago.setReservaId(1L);
        pago.setIdUsuario(1L);
        pago.setMonto(new BigDecimal("50000"));
        pago.setMetodoPago("TARJETA");
        pago.setEstadoPago(estadoPago);
        pago.setFechaPago(LocalDateTime.of(2026, 6, 21, 20, 0));
        return pago;
    }
}
