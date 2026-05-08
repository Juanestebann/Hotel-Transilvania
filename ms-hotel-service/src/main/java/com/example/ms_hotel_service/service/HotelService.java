package com.example.ms_hotel_service.service;

import com.example.ms_hotel_service.model.Hotel;
import com.example.ms_hotel_service.repository.HotelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class HotelService {

    private final HotelRepository hotelRepository;

    // Listar todos
    public List<Hotel> findAll() {
        return hotelRepository.findAll();
    }

    // Buscar por ID
    public Hotel findById(Long id) {
        return hotelRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Hotel no encontrado con id: " + id));
    }

    // Buscar por categoría
    public List<Hotel> findByCategoria(String categoria) {
        return hotelRepository.findByCategoria(categoria);
    }

    // Buscar por ciudad
    public List<Hotel> findByCiudad(String ciudad) {
        return hotelRepository.findByCiudad(ciudad);
    }

    // Buscar por país
    public List<Hotel> findByPais(String pais) {
        return hotelRepository.findByPais(pais);
    }

    // Guardar hotel
    public Hotel guardar(Hotel hotel) {
        hotel.setId(null);
        return hotelRepository.save(hotel);
    }

    // Actualizar hotel
    public Hotel actualizarHotel(Long id, Hotel hotel) {
        Hotel h = findById(id);

        h.setNombre(hotel.getNombre());
        h.setDireccion(hotel.getDireccion());
        h.setCiudad(hotel.getCiudad());
        h.setPais(hotel.getPais());
        h.setCategoria(hotel.getCategoria());
        h.setDescripcion(hotel.getDescripcion());

        return hotelRepository.save(h);
    }

    // Eliminar hotel
    public void eliminar(Long id) {
        Hotel hotel = findById(id);
        hotelRepository.delete(hotel);
    }
}