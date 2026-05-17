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

@RestController
@RequestMapping("/api/v1/habitaciones")
@RequiredArgsConstructor
public class HabitacionController {

    private final HabitacionService habitacionService;

    //http://localhost:8084/api/v1/habitaciones
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping
    public ResponseEntity<List<Habitacion>> findAll() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(habitacionService.findAll());
    }

    //http://localhost:8084/api/v1/habitaciones/1
    //http://localhost:8084/api/v1/habitaciones/9999
    @GetMapping("/{id}")
    public ResponseEntity<Habitacion> findById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(habitacionService.findById(id));
    }

    //http://localhost:8084/api/v1/habitaciones/estado/DISPONIBLE
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/estado/{estadoHabitacion}")
    public ResponseEntity<List<Habitacion>> findByEstadoHabitacion(
            @PathVariable String estadoHabitacion) {

        return ResponseEntity.ok(
                habitacionService.findByEstadoHabitacion(estadoHabitacion)
        );
    }

    //http://localhost:8084/api/v1/habitaciones/capacidad/2
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/capacidad/{capacidad}")
    public ResponseEntity<List<Habitacion>> findByCapacidadMinima(
            @PathVariable Integer capacidad) {

        return ResponseEntity.ok(
                habitacionService.findByCapacidadMinima(capacidad)
        );
    }

    //http://localhost:8084/api/v1/habitaciones/hotel/1
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/hotel/{idHotel}")
    public ResponseEntity<List<Habitacion>> findByIdHotel(
            @PathVariable Long idHotel) {

        return ResponseEntity.ok(
                habitacionService.findByIdHotel(idHotel)
        );
    }

    //http://localhost:8084/api/v1/habitaciones
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Habitacion> guardarHabitacion(
            @Valid @RequestBody Habitacion habitacion) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(habitacionService.guardar(habitacion));
    }

    //http://localhost:8084/api/v1/habitaciones/1
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Habitacion> actualizarHabitacion(
            @PathVariable Long id,
            @Valid @RequestBody Habitacion habitacion) {

        return ResponseEntity.status(HttpStatus.OK)
                .body(habitacionService.actualizar(id, habitacion));
    }

    //http://localhost:8084/api/v1/habitaciones/2
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarHabitacion(@PathVariable Long id) {

        habitacionService.eliminar(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .build();
    }
}