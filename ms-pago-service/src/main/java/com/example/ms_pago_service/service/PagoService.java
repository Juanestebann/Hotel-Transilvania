package com.example.ms_pago_service.service;

import com.example.ms_pago_service.client.ReservaClient;
import com.example.ms_pago_service.client.UsuarioClient;
import com.example.ms_pago_service.model.Pago;
import com.example.ms_pago_service.repository.PagoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class PagoService {

    private final PagoRepository pagoRepository;
    private final ReservaClient reservaClient;
    private final UsuarioClient usuarioClient;

    public List<Pago> findAll() {
        log.info("Listando todos los pagos");
        return pagoRepository.findAll();
    }

    public Pago findById(Long id) {
        log.info("Buscando pago con id: {}", id);

        return pagoRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Pago no encontrado con id: {}", id);
                    return new NoSuchElementException("Pago no encontrado con id: " + id);
                });
    }

    public Pago save(Pago pago) {
        log.info(
                "Intentando crear pago para reserva id: {}, usuario id: {}, monto: {}, estado: {}",
                pago.getReservaId(),
                pago.getIdUsuario(),
                pago.getMonto(),
                pago.getEstadoPago()
        );

        validarMonto(pago);
        validarEstadoPago(pago.getEstadoPago());

        pago.setEstadoPago(pago.getEstadoPago().toUpperCase());

        log.info("Validando existencia de reserva id: {}", pago.getReservaId());
        reservaClient.obtenerReservaPorId(pago.getReservaId());
        log.info("Reserva validada correctamente con id: {}", pago.getReservaId());

        log.info("Validando existencia de usuario id: {}", pago.getIdUsuario());
        usuarioClient.obtenerUsuarioPorId(pago.getIdUsuario());
        log.info("Usuario validado correctamente con id: {}", pago.getIdUsuario());

        actualizarEstadoReservaSegunPago(pago);

        Pago pagoGuardado = pagoRepository.save(pago);

        log.info(
                "Pago creado correctamente con id: {}, reserva id: {}, estado: {}",
                pagoGuardado.getIdPago(),
                pagoGuardado.getReservaId(),
                pagoGuardado.getEstadoPago()
        );

        return pagoGuardado;
    }

    public Pago update(Long id, Pago pagoActualizado) {
        log.info("Actualizando pago con id: {}", id);

        Pago pago = findById(id);

        validarMonto(pagoActualizado);
        validarEstadoPago(pagoActualizado.getEstadoPago());

        log.info("Validando existencia de reserva id: {}", pagoActualizado.getReservaId());
        reservaClient.obtenerReservaPorId(pagoActualizado.getReservaId());
        log.info("Reserva validada correctamente con id: {}", pagoActualizado.getReservaId());

        log.info("Validando existencia de usuario id: {}", pagoActualizado.getIdUsuario());
        usuarioClient.obtenerUsuarioPorId(pagoActualizado.getIdUsuario());
        log.info("Usuario validado correctamente con id: {}", pagoActualizado.getIdUsuario());

        pago.setReservaId(pagoActualizado.getReservaId());
        pago.setIdUsuario(pagoActualizado.getIdUsuario());
        pago.setMonto(pagoActualizado.getMonto());
        pago.setMetodoPago(pagoActualizado.getMetodoPago());
        pago.setEstadoPago(pagoActualizado.getEstadoPago().toUpperCase());
        pago.setFechaPago(pagoActualizado.getFechaPago());

        actualizarEstadoReservaSegunPago(pago);

        Pago pagoGuardado = pagoRepository.save(pago);

        log.info(
                "Pago actualizado correctamente con id: {}, reserva id: {}, estado: {}",
                pagoGuardado.getIdPago(),
                pagoGuardado.getReservaId(),
                pagoGuardado.getEstadoPago()
        );

        return pagoGuardado;
    }

    public void delete(Long id) {
        log.warn("Solicitando eliminación de pago con id: {}", id);

        Pago pago = findById(id);

        pagoRepository.delete(pago);

        log.warn("Pago eliminado correctamente con id: {}", id);
    }

    public List<Pago> findByReservaId(Long reservaId) {
        log.info("Buscando pagos asociados a reserva id: {}", reservaId);
        return pagoRepository.findByReservaId(reservaId);
    }

    public List<Pago> findByEstadoPago(String estadoPago) {
        log.info("Buscando pagos por estado: {}", estadoPago);
        return pagoRepository.findByEstadoPago(estadoPago.toUpperCase());
    }

    private void validarMonto(Pago pago) {
        log.info("Validando monto del pago: {}", pago.getMonto());

        if (pago.getMonto() == null || pago.getMonto().doubleValue() <= 0) {
            log.error("Monto inválido recibido para pago: {}", pago.getMonto());
            throw new IllegalArgumentException("El monto debe ser mayor a 0");
        }

        log.info("Monto validado correctamente: {}", pago.getMonto());
    }

    private void validarEstadoPago(String estadoPago) {
        log.info("Validando estado de pago: {}", estadoPago);

        if (estadoPago == null || estadoPago.isBlank()) {
            log.error("Estado de pago vacío o nulo");
            throw new IllegalArgumentException("El estado del pago no puede estar vacío");
        }

        String estado = estadoPago.toUpperCase();

        if (!estado.equals("PENDIENTE") &&
                !estado.equals("APROBADO") &&
                !estado.equals("RECHAZADO") &&
                !estado.equals("REEMBOLSADO") &&
                !estado.equals("ANULADO")) {

            log.error("Estado de pago inválido recibido: {}", estadoPago);

            throw new IllegalArgumentException(
                    "Estado de pago inválido. Debe ser PENDIENTE, APROBADO, RECHAZADO, REEMBOLSADO o ANULADO"
            );
        }

        log.info("Estado de pago validado correctamente: {}", estado);
    }

    private void actualizarEstadoReservaSegunPago(Pago pago) {
        String estadoPago = pago.getEstadoPago().toUpperCase();

        log.info(
                "Evaluando cambio de estado de reserva id: {} según estado de pago: {}",
                pago.getReservaId(),
                estadoPago
        );

        if (estadoPago.equals("APROBADO")) {
            log.warn("Pago aprobado. Cambiando reserva id: {} a CONFIRMADA", pago.getReservaId());
            reservaClient.cambiarEstadoReserva(pago.getReservaId(), "CONFIRMADA");
        }

        if (estadoPago.equals("RECHAZADO")) {
            log.warn("Pago rechazado. Cambiando reserva id: {} a CANCELADA", pago.getReservaId());
            reservaClient.cambiarEstadoReserva(pago.getReservaId(), "CANCELADA");
        }

        if (estadoPago.equals("REEMBOLSADO")) {
            log.warn("Pago reembolsado. Cambiando reserva id: {} a CANCELADA", pago.getReservaId());
            reservaClient.cambiarEstadoReserva(pago.getReservaId(), "CANCELADA");
        }

        if (estadoPago.equals("PENDIENTE")) {
            log.info("Pago pendiente. Cambiando reserva id: {} a PENDIENTE", pago.getReservaId());
            reservaClient.cambiarEstadoReserva(pago.getReservaId(), "PENDIENTE");
        }

        if (estadoPago.equals("ANULADO")) {
            log.warn("Pago anulado. No se cambia automáticamente el estado de la reserva id: {}", pago.getReservaId());
        }
    }
}