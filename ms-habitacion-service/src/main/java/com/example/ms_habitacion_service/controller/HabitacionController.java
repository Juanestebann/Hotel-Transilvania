package com.example.ms_habitacion_service.controller;
import com.example.ms_habitacion_service.model.Habitacion;
import com.example.ms_habitacion_service.service.HabitacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/habitaciones")
@RequiredArgsConstructor
public class HabitacionController {

    private final HabitacionService habitacionService;

    @GetMapping
    public ResponseEntity<List<Habitacion>> findAll() {
        return ResponseEntity.status(HttpStatus.OK).body(habitacionService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Habitacion> findById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(habitacionService.findById(id));
    }
    @GetMapping("/estado/{estadoHabitacion}")
    public ResponseEntity<List<Habitacion>> findByEstadoHabitacion(
            @PathVariable String estadoHabitacion) {

        return ResponseEntity.ok(
                habitacionService.findByEstadoHabitacion(estadoHabitacion)
        );
    }

    @GetMapping("/capacidad/{capacidad}")
    public ResponseEntity<List<Habitacion>> findByCapacidadMinima(
            @PathVariable Integer capacidad) {

        return ResponseEntity.ok(
                habitacionService.findByCapacidadMinima(capacidad)
        );
    }

    @GetMapping("/hotel/{idHotel}")
    public ResponseEntity<List<Habitacion>> findByIdHotel(
            @PathVariable Long idHotel) {

        return ResponseEntity.ok(
                habitacionService.findByIdHotel(idHotel)
        );
    }

    @PostMapping
    public ResponseEntity<Habitacion> guardarHabitacion(@Valid @RequestBody Habitacion habitacion) {
        return ResponseEntity.status(HttpStatus.CREATED).body(habitacionService.guardar(habitacion));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Habitacion> actualizarHabitacion(@PathVariable Long id,
                                                           @Valid @RequestBody Habitacion habitacion) {
        return ResponseEntity.status(HttpStatus.OK).body(habitacionService.actualizar(id, habitacion));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarHabitacion(@PathVariable Long id) {
        habitacionService.eliminar(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}