package com.example.ms_pago_service.repository;

import com.example.ms_pago_service.model.Pago;
import com.example.ms_pago_service.model.enums.EstadoPago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    List<Pago> findByReservaId(Long reservaId);
    List<Pago> findByEstadoPago(EstadoPago estadoPago);
}