package com.example.ms_pago_service.service;

import com.example.ms_pago_service.client.ReservaClient;
import com.example.ms_pago_service.client.UsuarioClient;
import com.example.ms_pago_service.model.Pago;
import com.example.ms_pago_service.repository.PagoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class PagoService {

    private final PagoRepository pagoRepository;
    private final ReservaClient reservaClient;
    private final UsuarioClient usuarioClient;

    public List<Pago> findAll() {
        return pagoRepository.findAll();
    }

    public Pago findById(Long id) {
        return pagoRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException("Pago no encontrado con id: " + id));
    }

    public Pago save(Pago pago) {

        validarMonto(pago);

        validarEstadoPago(pago.getEstadoPago());

        reservaClient.obtenerReservaPorId(pago.getReservaId());

        usuarioClient.obtenerUsuarioPorId(pago.getIdUsuario());

        return pagoRepository.save(pago);
    }

    public Pago update(Long id, Pago pagoActualizado) {

        Pago pago = findById(id);

        validarMonto(pagoActualizado);

        validarEstadoPago(pagoActualizado.getEstadoPago());

        reservaClient.obtenerReservaPorId(pagoActualizado.getReservaId());

        usuarioClient.obtenerUsuarioPorId(pagoActualizado.getIdUsuario());

        pago.setReservaId(pagoActualizado.getReservaId());
        pago.setIdUsuario(pagoActualizado.getIdUsuario());
        pago.setMonto(pagoActualizado.getMonto());
        pago.setMetodoPago(pagoActualizado.getMetodoPago());
        pago.setEstadoPago(pagoActualizado.getEstadoPago().toUpperCase());
        pago.setFechaPago(pagoActualizado.getFechaPago());

        return pagoRepository.save(pago);
    }

    public void delete(Long id) {
        Pago pago = findById(id);
        pagoRepository.delete(pago);
    }

    public List<Pago> findByReservaId(Long reservaId) {
        return pagoRepository.findByReservaId(reservaId);
    }

    public List<Pago> findByEstadoPago(String estadoPago) {
        return pagoRepository.findByEstadoPago(estadoPago.toUpperCase());
    }

    private void validarMonto(Pago pago) {
        if (pago.getMonto() == null || pago.getMonto().doubleValue() <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a 0");
        }
    }

    private void validarEstadoPago(String estadoPago) {
        if (estadoPago == null || estadoPago.isBlank()) {
            throw new IllegalArgumentException("El estado del pago no puede estar vacío");
        }

        String estado = estadoPago.toUpperCase();

        if (!estado.equals("PENDIENTE") &&
                !estado.equals("APROBADO") &&
                !estado.equals("RECHAZADO") &&
                !estado.equals("REEMBOLSADO") &&
                !estado.equals("ANULADO")) {
            throw new IllegalArgumentException(
                    "Estado de pago inválido. Debe ser PENDIENTE, APROBADO, RECHAZADO, REEMBOLSADO o ANULADO"
            );
        }
    }
}