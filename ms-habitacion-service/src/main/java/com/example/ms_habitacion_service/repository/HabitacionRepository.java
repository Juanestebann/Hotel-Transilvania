package com.example.ms_habitacion_service.repository;
import com.example.ms_habitacion_service.model.Habitacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HabitacionRepository extends JpaRepository<Habitacion, Long> {

}