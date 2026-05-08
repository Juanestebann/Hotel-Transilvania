package com.example.ms_hotel_service.repository;

import com.example.ms_hotel_service.model.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {

    List<Hotel> findByCategoria(String categoria);

    List<Hotel> findByCiudad(String ciudad);

    List<Hotel> findByPais(String pais);
}