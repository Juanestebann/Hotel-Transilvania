package com.example.ms_servicioAdicional_service.repository;

import com.example.ms_servicioAdicional_service.model.ServicioAdicionalModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServicioAdicionalRepository extends JpaRepository<ServicioAdicionalModel, Long> {

    // Obtener servicios adicionales por idHotel
    List<ServicioAdicionalModel> findByIdHotel(Long idHotel);

    // Obtener servicios adicionales por estado
    List<ServicioAdicionalModel> findByEstado(String estado);

    // Obtener servicios adicionales por nombre
    List<ServicioAdicionalModel> findByNombre(String nombre);

}