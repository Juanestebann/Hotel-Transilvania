package com.example.ms_reserva_service.repository;

import com.example.ms_reserva_service.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    // Obtener reservas por idCliente
    List<Reserva> findByIdCliente(Long idCliente);

    // Obtener reservas por idHotel
    List<Reserva> findByIdHotel(Long idHotel);

    //Obtener reservas por idUsuario
    List<Reserva> findByIdUsuario(Long idUsuario);

    // Obtener reservas por idHabitacion
    List<Reserva> findByIdHabitacion(Long idHabitacion);

    // Obtener reservas por estadoReserva
    List<Reserva> findByEstadoReserva(String estadoReserva);
}