package com.example.ms_disponibilidad_service.controller;
import com.example.ms_disponibilidad_service.model.Disponibilidad;
import com.example.ms_disponibilidad_service.service.DisponibilidadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/disponibilidades")
@RequiredArgsConstructor
public class DisponibilidadController {

    private final DisponibilidadService disponibilidadService;

    @GetMapping
    public ResponseEntity<List<Disponibilidad>> findAll() {
        return ResponseEntity.ok(disponibilidadService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Disponibilidad> findById(@PathVariable Long id) {
        return ResponseEntity.ok(disponibilidadService.findById(id));
    }
    @GetMapping("/habitacion/{idHabitacion}/fecha/{fecha}")
    public ResponseEntity<Disponibilidad> findByIdHabitacionAndFecha(
            @PathVariable Long idHabitacion,
            @PathVariable LocalDate fecha) {

        return ResponseEntity.ok(
                disponibilidadService.findByIdHabitacionAndFecha(idHabitacion, fecha)
        );
    }

    @PostMapping
    public ResponseEntity<Disponibilidad> guardar(@Valid @RequestBody Disponibilidad disponibilidad) {
        return ResponseEntity.status(HttpStatus.CREATED).body(disponibilidadService.guardar(disponibilidad));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Disponibilidad> actualizar(@PathVariable Long id, @Valid @RequestBody Disponibilidad disponibilidad) {
        return ResponseEntity.ok(disponibilidadService.actualizar(id, disponibilidad));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        disponibilidadService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}