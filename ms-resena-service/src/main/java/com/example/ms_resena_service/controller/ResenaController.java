package com.example.ms_resena_service.controller;

import com.example.ms_resena_service.model.Resena;
import com.example.ms_resena_service.service.ResenaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/resenas")
public class ResenaController {

    private final ResenaService resenaService;

    public ResenaController(ResenaService resenaService) {
        this.resenaService = resenaService;
    }

    @GetMapping
    public ResponseEntity<List<Resena>> listarResenas() {
        return ResponseEntity.ok(resenaService.listarResenas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resena> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(resenaService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<Resena> guardarResena(@Valid @RequestBody Resena resena) {
        return ResponseEntity.ok(resenaService.guardarResena(resena));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Resena> actualizarResena(
            @PathVariable Long id,
            @Valid @RequestBody Resena resena
    ) {
        return ResponseEntity.ok(resenaService.actualizarResena(id, resena));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminarResena(@PathVariable Long id) {
        resenaService.eliminarResena(id);
        return ResponseEntity.ok("Reseña eliminada correctamente");
    }

    @GetMapping("/cliente/{idCliente}")
    public ResponseEntity<List<Resena>> buscarPorCliente(@PathVariable Long idCliente) {
        return ResponseEntity.ok(resenaService.buscarPorCliente(idCliente));
    }

    @GetMapping("/hotel/{idHotel}")
    public ResponseEntity<List<Resena>> buscarPorHotel(@PathVariable Long idHotel) {
        return ResponseEntity.ok(resenaService.buscarPorHotel(idHotel));
    }

    @GetMapping("/habitacion/{idHabitacion}")
    public ResponseEntity<List<Resena>> buscarPorHabitacion(@PathVariable Long idHabitacion) {
        return ResponseEntity.ok(resenaService.buscarPorHabitacion(idHabitacion));
    }

    @GetMapping("/reserva/{idReserva}")
    public ResponseEntity<List<Resena>> buscarPorReserva(@PathVariable Long idReserva) {
        return ResponseEntity.ok(resenaService.buscarPorReserva(idReserva));
    }

    @GetMapping("/calificacion/{calificacion}")
    public ResponseEntity<List<Resena>> buscarPorCalificacion(@PathVariable Integer calificacion) {
        return ResponseEntity.ok(resenaService.buscarPorCalificacion(calificacion));
    }

    @GetMapping("/estado/{estadoResena}")
    public ResponseEntity<List<Resena>> buscarPorEstado(@PathVariable String estadoResena) {
        return ResponseEntity.ok(resenaService.buscarPorEstado(estadoResena));
    }
}