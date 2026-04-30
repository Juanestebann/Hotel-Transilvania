package com.example.ms_hotel_service.controller;

import com.example.ms_hotel_service.model.Hotel;
import com.example.ms_hotel_service.service.HotelService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("api/v1/hoteles")
public class HotelController {
    @Autowired
    private HotelService hotelService;

    @GetMapping
    public ResponseEntity<List<Hotel>> findAll() {
        List<Hotel> hoteles = hotelService.findAll();
        return ResponseEntity.ok(hoteles);
    }
    @GetMapping("/id/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id){
        try{
            Hotel hotel = hotelService.findById(id);
            return ResponseEntity.status(HttpStatus.OK).body(hotel);

        }catch (NoSuchElementException ex){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ex.getMessage());
        }
    }


    /*
    //http://localhost:9090/api/v1/hoteles?id=2
    @GetMapping(params="id")
    public ResponseEntity<Hotel> findById(@RequestParam Long id){
        return hotelService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }*/
    //http://localhost:9090/api/v1/hoteles?categoria=3 estrellas
    @GetMapping(params ="categoria")
    public ResponseEntity<List<Hotel>> findByCategoria(@RequestParam String categoria){
        List<Hotel> hotel = hotelService.findByCategoria(categoria);
        return ResponseEntity.ok(hotel);
    }

    @PostMapping
    public ResponseEntity<Hotel> guardarHotel(@Valid @RequestBody Hotel hotel){
        Hotel h = hotelService.guardar(hotel);
        return ResponseEntity.status(HttpStatus.CREATED).body(h);
    }
    @PutMapping("/{id}")
    public ResponseEntity<Hotel> actualizarHotel(
            @PathVariable Long id,
            @Valid @RequestBody Hotel hotel){

        Hotel hotelActualizado = hotelService.actualizarHotel(id, hotel);

        return ResponseEntity.ok(hotelActualizado);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarHotel(@PathVariable Long id){

        hotelService.eliminar(id);

        return ResponseEntity.noContent().build();
    }


}
