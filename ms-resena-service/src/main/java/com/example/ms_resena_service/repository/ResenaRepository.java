package com.example.ms_resena_service.repository;

import com.example.ms_resena_service.model.Resena;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResenaRepository extends JpaRepository<Resena, Long> {

    List<Resena> findByIdCliente(Long idCliente);

    List<Resena> findByIdHotel(Long idHotel);

    List<Resena> findByIdReserva(Long idReserva);

    List<Resena> findByCalificacion(Integer calificacion);

    List<Resena> findByEstadoResena(String estadoResena);
}