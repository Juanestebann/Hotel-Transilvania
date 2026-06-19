package com.example.ms_habitacion_service.controller;

import com.example.ms_habitacion_service.model.Habitacion;
import com.example.ms_habitacion_service.service.HabitacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/v1/habitaciones")
@RequiredArgsConstructor
public class HabitacionController {

    private final HabitacionService habitacionService;

    @GetMapping
    public ResponseEntity<List<Habitacion>> findAll() {
        List<Habitacion> habitaciones = habitacionService.findAll();

        habitaciones.forEach(this::agregarLinksHabitacion);

        return ResponseEntity.status(HttpStatus.OK).body(habitaciones);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Habitacion> findById(@PathVariable Long id) {
        Habitacion habitacion = habitacionService.findById(id);

        agregarLinksHabitacion(habitacion);

        return ResponseEntity.status(HttpStatus.OK).body(habitacion);
    }

    @GetMapping("/estado/{estadoHabitacion}")
    public ResponseEntity<List<Habitacion>> findByEstadoHabitacion(
            @PathVariable String estadoHabitacion) {

        List<Habitacion> habitaciones = habitacionService.findByEstadoHabitacion(estadoHabitacion);

        habitaciones.forEach(this::agregarLinksHabitacion);

        return ResponseEntity.ok(habitaciones);
    }

    @GetMapping("/capacidad/{capacidad}")
    public ResponseEntity<List<Habitacion>> findByCapacidadMinima(
            @PathVariable Integer capacidad) {

        List<Habitacion> habitaciones = habitacionService.findByCapacidadMinima(capacidad);

        habitaciones.forEach(this::agregarLinksHabitacion);

        return ResponseEntity.ok(habitaciones);
    }

    @GetMapping("/hotel/{idHotel}")
    public ResponseEntity<List<Habitacion>> findByIdHotel(
            @PathVariable Long idHotel) {

        List<Habitacion> habitaciones = habitacionService.findByIdHotel(idHotel);

        habitaciones.forEach(this::agregarLinksHabitacion);

        return ResponseEntity.ok(habitaciones);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Habitacion> guardarHabitacion(
            @Valid @RequestBody Habitacion habitacion) {

        Habitacion habitacionGuardada = habitacionService.guardar(habitacion);

        agregarLinksHabitacion(habitacionGuardada);

        return ResponseEntity.status(HttpStatus.CREATED).body(habitacionGuardada);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Habitacion> actualizarHabitacion(
            @PathVariable Long id,
            @Valid @RequestBody Habitacion habitacion) {

        Habitacion habitacionActualizada = habitacionService.actualizar(id, habitacion);

        agregarLinksHabitacion(habitacionActualizada);

        return ResponseEntity.status(HttpStatus.OK).body(habitacionActualizada);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarHabitacion(@PathVariable Long id) {
        habitacionService.eliminar(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    private void agregarLinksHabitacion(Habitacion habitacion) {

        habitacion.removeLinks();

        habitacion.add(linkTo(methodOn(HabitacionController.class)
                .findById(habitacion.getIdHabitacion())).withSelfRel());

        habitacion.add(linkTo(methodOn(HabitacionController.class)
                .findAll()).withRel("todas-las-habitaciones"));

        habitacion.add(linkTo(methodOn(HabitacionController.class)
                .findByEstadoHabitacion(habitacion.getEstadoHabitacion())).withRel("buscar-por-estado"));

        habitacion.add(linkTo(methodOn(HabitacionController.class)
                .findByCapacidadMinima(habitacion.getCapacidad())).withRel("buscar-por-capacidad"));

        habitacion.add(linkTo(methodOn(HabitacionController.class)
                .findByIdHotel(habitacion.getIdHotel())).withRel("buscar-por-hotel"));

        habitacion.add(linkTo(methodOn(HabitacionController.class)
                .guardarHabitacion(null)).withRel("crear-habitacion"));

        habitacion.add(linkTo(methodOn(HabitacionController.class)
                .actualizarHabitacion(habitacion.getIdHabitacion(), null)).withRel("actualizar-habitacion"));

        habitacion.add(linkTo(methodOn(HabitacionController.class)
                .eliminarHabitacion(habitacion.getIdHabitacion())).withRel("eliminar-habitacion"));
    }
}