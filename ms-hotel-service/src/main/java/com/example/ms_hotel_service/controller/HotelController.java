package com.example.ms_hotel_service.controller;

import com.example.ms_hotel_service.model.Hotel;
import com.example.ms_hotel_service.service.HotelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/v1/hoteles")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;

    //http://localhost:8083/api/v1/hoteles
    @GetMapping
    public ResponseEntity<List<Hotel>> findAll() {
        List<Hotel> hoteles = hotelService.findAll();
        hoteles.forEach(this::agregarLinksHotel);
        return ResponseEntity.ok(hoteles);
    }

    //http://localhost:8083/api/v1/hoteles/1
    //http://localhost:8083/api/v1/hoteles/12312

    @GetMapping("/{id}")
    public ResponseEntity<Hotel> findById(@PathVariable Long id) {
        Hotel hotel = hotelService.findById(id);
        agregarLinksHotel(hotel);
        return ResponseEntity.ok(hotel);
    }

    //http://localhost:8083/api/v1/hoteles?categoria=5 estrellas
    @GetMapping(params = "categoria")
    public ResponseEntity<List<Hotel>> findByCategoria(@RequestParam String categoria) {
        List<Hotel> hoteles = hotelService.findByCategoria(categoria);
        hoteles.forEach(this::agregarLinksHotel);
        return ResponseEntity.ok(hoteles);
    }

    //http://localhost:8083/api/v1/hoteles?ciudad=Santiago
    @GetMapping(params = "ciudad")
    public ResponseEntity<List<Hotel>> findByCiudad(@RequestParam String ciudad) {
        List<Hotel> hoteles = hotelService.findByCiudad(ciudad);
        hoteles.forEach(this::agregarLinksHotel);
        return ResponseEntity.ok(hoteles);
    }

    //http://localhost:8083/api/v1/hoteles?pais=Chile
    @GetMapping(params = "pais")
    public ResponseEntity<List<Hotel>> findByPais(@RequestParam String pais) {
        List<Hotel> hoteles = hotelService.findByPais(pais);
        hoteles.forEach(this::agregarLinksHotel);
        return ResponseEntity.ok(hoteles);
    }

    //http://localhost:8083/api/v1/hoteles
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Hotel> guardarHotel(@Valid @RequestBody Hotel hotel) {
        Hotel nuevoHotel = hotelService.guardar(hotel);
        agregarLinksHotel(nuevoHotel);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(nuevoHotel);
    }

    //http://localhost:8083/api/v1/hoteles/1
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Hotel> actualizarHotel(
            @PathVariable Long id,
            @Valid @RequestBody Hotel hotel) {

        Hotel hotelActualizado = hotelService.actualizarHotel(id, hotel);
        agregarLinksHotel(hotelActualizado);
        return ResponseEntity.ok(hotelActualizado);
    }

    //http://localhost:8083/api/v1/hoteles/6
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarHotel(@PathVariable Long id) {
        hotelService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    private void agregarLinksHotel(Hotel hotel) {
        hotel.add(linkTo(methodOn(HotelController.class)
                .findById(hotel.getId())).withSelfRel());
        hotel.add(linkTo(methodOn(HotelController.class)
                .findAll()).withRel("listar-todos"));
        hotel.add(linkTo(methodOn(HotelController.class)
                .guardarHotel(null)).withRel("crear"));
        hotel.add(linkTo(methodOn(HotelController.class)
                .actualizarHotel(hotel.getId(), null)).withRel("actualizar"));
        hotel.add(linkTo(methodOn(HotelController.class)
                .eliminarHotel(hotel.getId())).withRel("eliminar"));
        hotel.add(linkTo(methodOn(HotelController.class)
                .findByCategoria(hotel.getCategoria())).withRel("buscar-por-categoria"));
        hotel.add(linkTo(methodOn(HotelController.class)
                .findByCiudad(hotel.getCiudad())).withRel("buscar-por-ciudad"));
        hotel.add(linkTo(methodOn(HotelController.class)
                .findByPais(hotel.getPais())).withRel("buscar-por-pais"));
    }
}
