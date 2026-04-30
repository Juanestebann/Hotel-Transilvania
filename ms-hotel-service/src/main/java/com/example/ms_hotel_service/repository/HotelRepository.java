package com.example.ms_hotel_service.repository;

import com.example.ms_hotel_service.model.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface HotelRepository extends JpaRepository <Hotel, Long> {

    //Obtener todos los hoteles de la misma categoria.
    List<Hotel> findByCategoria(String categoria);
}
