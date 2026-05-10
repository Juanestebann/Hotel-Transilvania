package com.example.ms_habitacion_service.repository;
import com.example.ms_habitacion_service.model.Habitacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HabitacionRepository extends JpaRepository<Habitacion, Long> {

    List<Habitacion> findByEstadoHabitacion(String estadoHabitacion);

    List<Habitacion> findByCapacidadGreaterThanEqual(Integer capacidad);

    List<Habitacion> findByIdHotel(Long idHotel);
}