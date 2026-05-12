package com.example.ms_notificacion_service.repository;

import com.example.ms_notificacion_service.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    // Obtener notificaciones por idCliente
    List<Notificacion> findByIdCliente(Long idCliente);

    // Obtener notificaciones por idUsuario
    List<Notificacion> findByIdUsuario(Long idUsuario);

    // Obtener notificaciones por idReserva
    List<Notificacion> findByIdReserva(Long idReserva);
}