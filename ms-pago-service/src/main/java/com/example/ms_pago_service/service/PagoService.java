package com.example.ms_pago_service.service;
import com.example.ms_pago_service.client.ReservaClient;
import com.example.ms_pago_service.client.UsuarioClient;
import com.example.ms_pago_service.model.Pago;
import com.example.ms_pago_service.model.enums.EstadoPago;
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

        if (pago.getMonto().doubleValue() <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a 0");
        }

        reservaClient.obtenerReservaPorId(pago.getReservaId());

        usuarioClient.obtenerUsuarioPorId(pago.getIdUsuario());

        return pagoRepository.save(pago);
    }

    public Pago update(Long id, Pago pagoActualizado) {

        Pago pago = findById(id);

        if (pagoActualizado.getMonto().doubleValue() <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a 0");
        }

        reservaClient.obtenerReservaPorId(pagoActualizado.getReservaId());

        usuarioClient.obtenerUsuarioPorId(pagoActualizado.getIdUsuario());

        pago.setReservaId(pagoActualizado.getReservaId());
        pago.setIdUsuario(pagoActualizado.getIdUsuario());
        pago.setMonto(pagoActualizado.getMonto());
        pago.setMetodoPago(pagoActualizado.getMetodoPago());
        pago.setEstadoPago(pagoActualizado.getEstadoPago());
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

    public List<Pago> findByEstadoPago(EstadoPago estadoPago) {
        return pagoRepository.findByEstadoPago(estadoPago);
    }
}