package com.example.ms_hotel_service.service;

import com.example.ms_hotel_service.model.Hotel;
import com.example.ms_hotel_service.repository.HotelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotelService {

    private final HotelRepository hotelRepository;

    // Listar todos
    public List<Hotel> findAll() {
        log.info("Listando todos los hoteles");
        return hotelRepository.findAll();
    }

    // Buscar por ID
    public Hotel findById(Long id) {

        log.info("Buscando hotel con id: {}", id);

        return hotelRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Hotel no encontrado con id: {}", id);
                    return new NoSuchElementException("Hotel no encontrado con id: " + id);
                });
    }

    // Buscar por categoría
    public List<Hotel> findByCategoria(String categoria) {

        log.info("Buscando hoteles por categoría: {}", categoria);

        return hotelRepository.findByCategoria(categoria);
    }

    // Buscar por ciudad
    public List<Hotel> findByCiudad(String ciudad) {

        log.info("Buscando hoteles por ciudad: {}", ciudad);

        return hotelRepository.findByCiudad(ciudad);
    }

    // Buscar por país
    public List<Hotel> findByPais(String pais) {

        log.info("Buscando hoteles por país: {}", pais);

        return hotelRepository.findByPais(pais);
    }

    // Guardar hotel
    public Hotel guardar(Hotel hotel) {

        log.info(
                "Creando hotel: {} en ciudad: {}",
                hotel.getNombre(),
                hotel.getCiudad()
        );

        hotel.setId(null);

        Hotel hotelGuardado = hotelRepository.save(hotel);

        log.info(
                "Hotel creado correctamente con id: {}",
                hotelGuardado.getId()
        );

        return hotelGuardado;
    }

    // Actualizar hotel
    public Hotel actualizarHotel(Long id, Hotel hotel) {

        log.info("Actualizando hotel con id: {}", id);

        Hotel h = findById(id);

        h.setNombre(hotel.getNombre());
        h.setDireccion(hotel.getDireccion());
        h.setCiudad(hotel.getCiudad());
        h.setPais(hotel.getPais());
        h.setCategoria(hotel.getCategoria());
        h.setDescripcion(hotel.getDescripcion());

        Hotel hotelActualizado = hotelRepository.save(h);

        log.info(
                "Hotel actualizado correctamente con id: {}",
                hotelActualizado.getId()
        );

        return hotelActualizado;
    }

    // Eliminar hotel
    public void eliminar(Long id) {

        log.warn("Solicitando eliminación de hotel con id: {}", id);

        Hotel hotel = findById(id);

        hotelRepository.delete(hotel);

        log.warn("Hotel eliminado correctamente con id: {}", id);
    }
}