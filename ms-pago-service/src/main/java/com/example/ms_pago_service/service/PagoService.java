package com.example.ms_pago_service.service;

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

    public List<Pago> findAll() {
        return pagoRepository.findAll();
    }

    public Pago findById(Long id) {
        return pagoRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException("Pago no encontrado con id: " + id));
    }

    public Pago save(Pago pago) {
        if(pago.getMonto().doubleValue() <= 0){
            throw new IllegalArgumentException("El monto debe ser mayor a 0");
        }
        return pagoRepository.save(pago);
    }

    public Pago update(Long id, Pago pagoActualizado) {

        Pago pago = findById(id);

        pago.setReservaId(pagoActualizado.getReservaId());
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

    public List<Pago> findByEstadoPago(String estadoPago) {
        return pagoRepository.findByEstadoPago(estadoPago);
    }
}