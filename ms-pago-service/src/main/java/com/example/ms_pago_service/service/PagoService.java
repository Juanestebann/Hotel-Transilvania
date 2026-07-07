package com.example.ms_pago_service.service;

import com.example.ms_pago_service.client.ReservaClient;
import com.example.ms_pago_service.client.UsuarioClient;
import com.example.ms_pago_service.dto.ReservaDTO;
import com.example.ms_pago_service.dto.UsuarioDTO;
import com.example.ms_pago_service.model.Pago;
import com.example.ms_pago_service.repository.PagoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

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

        UsuarioDTO usuarioActual = usuarioClient.obtenerUsuarioActual();
        Pago pago = buscarPorId(id);

        validarAccesoAPago(usuarioActual, pago);

        return pago;
    }

    private Pago buscarPorId(Long id) {
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

        if (pago.getFechaPago() == null) {
            pago.setFechaPago(LocalDateTime.now());
        }

        validarMonto(pago);
        validarEstadoPago(pago.getEstadoPago());

        pago.setEstadoPago(pago.getEstadoPago().toUpperCase());

        UsuarioDTO usuarioActual = usuarioClient.obtenerUsuarioActual();
        boolean validarUsuarioDestino = prepararIdentidadPago(pago, usuarioActual);

        log.info("Validando existencia de reserva id: {}", pago.getReservaId());
        ReservaDTO reserva = reservaClient.obtenerReservaPorId(pago.getReservaId());
        validarPropiedadReserva(pago, reserva, usuarioActual);
        log.info("Reserva validada correctamente con id: {}", pago.getReservaId());

        if (validarUsuarioDestino) {
            log.info("Validando existencia de usuario destino id: {}", pago.getIdUsuario());
            usuarioClient.obtenerUsuarioPorId(pago.getIdUsuario());
        }

        validarReservaNoPagada(pago.getReservaId());

        Pago pagoGuardado = pagoRepository.save(pago);
        actualizarEstadoReservaSegunPago(pagoGuardado);

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

        Pago pago = buscarPorId(id);

        if (pagoActualizado.getFechaPago() == null) {
            pagoActualizado.setFechaPago(LocalDateTime.now());
        }

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

        Pago pagoGuardado = pagoRepository.save(pago);
        actualizarEstadoReservaSegunPago(pagoGuardado);

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

        Pago pago = buscarPorId(id);

        pagoRepository.delete(pago);

        log.warn("Pago eliminado correctamente con id: {}", id);
    }

    public List<Pago> findByReservaId(Long reservaId) {
        log.info("Buscando pagos asociados a reserva id: {}", reservaId);

        UsuarioDTO usuarioActual = usuarioClient.obtenerUsuarioActual();

        if (esRol(usuarioActual, "ADMIN")) {
            return pagoRepository.findByReservaId(reservaId);
        }

        if (!esRol(usuarioActual, "USER")) {
            throw accesoDenegado("Rol no autorizado para consultar pagos");
        }

        ReservaDTO reserva = reservaClient.obtenerReservaPorId(reservaId);

        if (reserva == null
                || !Objects.equals(reserva.getIdUsuario(), usuarioActual.getIdUsuario())) {
            throw accesoDenegado("No puedes consultar pagos de una reserva de otro usuario");
        }

        List<Pago> pagos = pagoRepository.findByReservaId(reservaId);

        boolean contienePagosAjenos = pagos.stream()
                .anyMatch(pago -> !Objects.equals(
                        pago.getIdUsuario(),
                        usuarioActual.getIdUsuario()
                ));

        if (contienePagosAjenos) {
            throw accesoDenegado("La reserva contiene pagos asociados a otro usuario");
        }

        return pagos;
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

        if (estadoPago.equals("ANULADO")) {
            log.warn("Pago anulado. No se cambia automáticamente el estado de la reserva id: {}", pago.getReservaId());
        }
    }

    private boolean prepararIdentidadPago(Pago pago, UsuarioDTO usuarioActual) {
        if (esRol(usuarioActual, "ADMIN")) {
            return true;
        }

        if (!esRol(usuarioActual, "USER")) {
            throw accesoDenegado("Rol no autorizado para crear pagos");
        }

        if (!Objects.equals(usuarioActual.getIdUsuario(), pago.getIdUsuario())) {
            throw accesoDenegado("No puedes crear un pago para otro usuario");
        }

        pago.setIdUsuario(usuarioActual.getIdUsuario());
        return false;
    }

    private void validarPropiedadReserva(
            Pago pago,
            ReservaDTO reserva,
            UsuarioDTO usuarioActual
    ) {
        if (!esRol(usuarioActual, "USER")) {
            return;
        }

        if (reserva == null
                || !Objects.equals(reserva.getIdUsuario(), usuarioActual.getIdUsuario())
                || !Objects.equals(reserva.getIdUsuario(), pago.getIdUsuario())) {
            throw accesoDenegado("No puedes pagar una reserva de otro usuario");
        }
    }

    private void validarAccesoAPago(UsuarioDTO usuarioActual, Pago pago) {
        if (esRol(usuarioActual, "ADMIN")) {
            return;
        }

        if (!esRol(usuarioActual, "USER")
                || !Objects.equals(usuarioActual.getIdUsuario(), pago.getIdUsuario())) {
            throw accesoDenegado("No puedes consultar un pago de otro usuario");
        }
    }

    private void validarReservaNoPagada(Long reservaId) {
        boolean yaPagada = pagoRepository.findByReservaId(reservaId).stream()
                .anyMatch(pago -> "APROBADO".equalsIgnoreCase(pago.getEstadoPago()));

        if (yaPagada) {
            throw new IllegalArgumentException("La reserva ya tiene un pago aprobado");
        }
    }

    private boolean esRol(UsuarioDTO usuario, String rolEsperado) {
        if (usuario == null || usuario.getRol() == null) {
            return false;
        }

        String rol = usuario.getRol().toUpperCase();
        return rolEsperado.equals(rol) || ("ROLE_" + rolEsperado).equals(rol);
    }

    private ResponseStatusException accesoDenegado(String mensaje) {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, mensaje);
    }
}
