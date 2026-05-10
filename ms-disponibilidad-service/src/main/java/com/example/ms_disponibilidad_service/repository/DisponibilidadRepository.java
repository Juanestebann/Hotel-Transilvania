package com.example.ms_disponibilidad_service.repository;

import com.example.ms_disponibilidad_service.model.Disponibilidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DisponibilidadRepository extends JpaRepository<Disponibilidad, Long> {

    List<Disponibilidad> findByIdHabitacion(Long idHabitacion);

    Optional<Disponibilidad> findByIdHabitacionAndFecha(
            Long idHabitacion,
            LocalDate fecha
    );

    List<Disponibilidad> findByFecha(LocalDate fecha);

    List<Disponibilidad> findByEstado(String estado);
}