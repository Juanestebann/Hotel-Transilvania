package com.example.ms_disponibilidad_service.repository;
import com.example.ms_disponibilidad_service.model.Disponibilidad;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DisponibilidadRepository extends JpaRepository<Disponibilidad, Long> {
}