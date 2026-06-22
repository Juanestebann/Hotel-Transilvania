package com.example.ms_pago_service.service;

import com.example.ms_pago_service.client.ReservaClient;
import com.example.ms_pago_service.client.UsuarioClient;
import com.example.ms_pago_service.model.Pago;
import com.example.ms_pago_service.repository.PagoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

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
    void deberiaRetornarPagoCuandoExiste() {
        Pago pago = crearPago("APROBADO");

        Mockito.when(pagoRepository.findById(1L)).thenReturn(Optional.of(pago));

        Pago resultado = pagoService.findById(1L);

        assertEquals(1L, resultado.getIdPago());
        assertEquals("APROBADO", resultado.getEstadoPago());
        verify(pagoRepository).findById(1L);
    }

    @Test
    void deberiaLanzarErrorCuandoPagoNoExiste() {
        Mockito.when(pagoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> pagoService.findById(99L));

        verify(pagoRepository).findById(99L);
    }

    @Test
    void deberiaCrearPagoAprobadoYCambiarReservaAConfirmada() {
        Pago pago = crearPago("APROBADO");

        Mockito.when(pagoRepository.save(pago)).thenReturn(pago);

        Pago resultado = pagoService.save(pago);

        assertEquals("APROBADO", resultado.getEstadoPago());
        verify(reservaClient).obtenerReservaPorId(1L);
        verify(usuarioClient).obtenerUsuarioPorId(1L);
        verify(pagoRepository).save(pago);
        verify(reservaClient).cambiarEstadoReserva(1L, "CONFIRMADA");
    }

    @Test
    void deberiaCambiarReservaACanceladaCuandoPagoEsRechazado() {
        Pago pago = crearPago("RECHAZADO");

        Mockito.when(pagoRepository.save(pago)).thenReturn(pago);

        pagoService.save(pago);

        verify(reservaClient).cambiarEstadoReserva(1L, "CANCELADA");
    }

    @Test
    void deberiaCambiarReservaACanceladaCuandoPagoEsReembolsado() {
        Pago pago = crearPago("REEMBOLSADO");

        Mockito.when(pagoRepository.save(pago)).thenReturn(pago);

        pagoService.save(pago);

        verify(reservaClient).cambiarEstadoReserva(1L, "CANCELADA");
    }

    @Test
    void deberiaCambiarReservaAPendienteCuandoPagoEsPendiente() {
        Pago pago = crearPago("PENDIENTE");

        Mockito.when(pagoRepository.save(pago)).thenReturn(pago);

        pagoService.save(pago);

        verify(reservaClient).cambiarEstadoReserva(1L, "PENDIENTE");
    }

    @Test
    void noDeberiaCambiarReservaCuandoPagoEsAnulado() {
        Pago pago = crearPago("ANULADO");

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
    void deberiaBuscarPagosPorReservaId() {
        Mockito.when(pagoRepository.findByReservaId(1L)).thenReturn(List.of(crearPago("APROBADO")));

        List<Pago> resultado = pagoService.findByReservaId(1L);

        assertEquals(1, resultado.size());
        verify(pagoRepository).findByReservaId(1L);
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