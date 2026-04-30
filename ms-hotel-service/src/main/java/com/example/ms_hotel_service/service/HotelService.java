package com.example.ms_hotel_service.service;

import com.example.ms_hotel_service.model.Hotel;
import com.example.ms_hotel_service.repository.HotelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class HotelService {
    @Autowired
    private HotelRepository hotelRepository;
    //Listar todos
    public List<Hotel> findAll(){
        return hotelRepository.findAll();
    }

    public List<Hotel> findByCategoria(String categoria){
        return hotelRepository.findByCategoria(categoria);
    }
    //Obtener por Id
    /*
    public Optional<Hotel> findById(Long id){
        return hotelRepository.findById(id);
    }
    */
    public Hotel findById(Long id) {
        return hotelRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Hotel no encontrado con id: " + id));
    }

    //Guardar/Modificar
    public Hotel guardar(Hotel hotel){
        return hotelRepository.save(hotel);
    }

    public Hotel actualizarHotel(Long id, Hotel hotel) {

        Hotel h = hotelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hotel no encontrado"));

        h.setNombre(hotel.getNombre());
        h.setDireccion(hotel.getDireccion());
        h.setCiudad(hotel.getCiudad());
        h.setPais(hotel.getPais());
        h.setCategoria(hotel.getCategoria());
        h.setDescripcion(hotel.getDescripcion());

        return hotelRepository.save(h);
    }



    //Delete
    public String eliminar(Long id){
        hotelRepository.deleteById(id);
        return "Hotel Eliminado: " + id;
    }


}
