package com.example.ms_hotel_service.controller;

import com.example.ms_hotel_service.model.Hotel;
import com.example.ms_hotel_service.service.HotelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hoteles")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;

    @GetMapping
    public ResponseEntity<List<Hotel>> findAll() {
        return ResponseEntity.ok(hotelService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Hotel> findById(@PathVariable Long id) {
        return ResponseEntity.ok(hotelService.findById(id));
    }

    @GetMapping(params = "categoria")
    public ResponseEntity<List<Hotel>> findByCategoria(@RequestParam String categoria) {
        return ResponseEntity.ok(hotelService.findByCategoria(categoria));
    }

    @GetMapping(params = "ciudad")
    public ResponseEntity<List<Hotel>> findByCiudad(@RequestParam String ciudad) {
        return ResponseEntity.ok(hotelService.findByCiudad(ciudad));
    }

    @GetMapping(params = "pais")
    public ResponseEntity<List<Hotel>> findByPais(@RequestParam String pais) {
        return ResponseEntity.ok(hotelService.findByPais(pais));
    }

    @PostMapping
    public ResponseEntity<Hotel> guardarHotel(@Valid @RequestBody Hotel hotel) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(hotelService.guardar(hotel));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Hotel> actualizarHotel(
            @PathVariable Long id,
            @Valid @RequestBody Hotel hotel) {

        return ResponseEntity.ok(hotelService.actualizarHotel(id, hotel));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarHotel(@PathVariable Long id) {
        hotelService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}