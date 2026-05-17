package com.example.ms_resena_service.controller;

import com.example.ms_resena_service.model.Resena;
import com.example.ms_resena_service.service.ResenaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/resenas")
public class ResenaController {

    private final ResenaService resenaService;

    public ResenaController(ResenaService resenaService) {
        this.resenaService = resenaService;
    }

    // http://localhost:8089/api/v1/resenas
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping
    public ResponseEntity<List<Resena>> listarResenas() {
        return ResponseEntity.ok(resenaService.listarResenas());
    }

    // http://localhost:8089/api/v1/resenas/1
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<Resena> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(resenaService.buscarPorId(id));
    }

    // http://localhost:8089/api/v1/resenas
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<Resena> guardarResena(
            @Valid @RequestBody Resena resena
    ) {

        return ResponseEntity.ok(
                resenaService.guardarResena(resena)
        );
    }

    // http://localhost:8089/api/v1/resenas/1
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Resena> actualizarResena(
            @PathVariable Long id,
            @Valid @RequestBody Resena resena
    ) {

        return ResponseEntity.ok(
                resenaService.actualizarResena(id, resena)
        );
    }

    // http://localhost:8089/api/v1/resenas/1
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminarResena(@PathVariable Long id) {

        resenaService.eliminarResena(id);

        return ResponseEntity.ok(
                "Reseña eliminada correctamente"
        );
    }

    // http://localhost:8089/api/v1/resenas/cliente/1
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/cliente/{idCliente}")
    public ResponseEntity<List<Resena>> buscarPorCliente(
            @PathVariable Long idCliente
    ) {

        return ResponseEntity.ok(
                resenaService.buscarPorCliente(idCliente)
        );
    }

    // http://localhost:8089/api/v1/resenas/hotel/1
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/hotel/{idHotel}")
    public ResponseEntity<List<Resena>> buscarPorHotel(
            @PathVariable Long idHotel
    ) {

        return ResponseEntity.ok(
                resenaService.buscarPorHotel(idHotel)
        );
    }

    // http://localhost:8089/api/v1/resenas/habitacion/1
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/habitacion/{idHabitacion}")
    public ResponseEntity<List<Resena>> buscarPorHabitacion(
            @PathVariable Long idHabitacion
    ) {

        return ResponseEntity.ok(
                resenaService.buscarPorHabitacion(idHabitacion)
        );
    }

    // http://localhost:8089/api/v1/resenas/reserva/1
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/reserva/{idReserva}")
    public ResponseEntity<List<Resena>> buscarPorReserva(
            @PathVariable Long idReserva
    ) {

        return ResponseEntity.ok(
                resenaService.buscarPorReserva(idReserva)
        );
    }

    // http://localhost:8089/api/v1/resenas/calificacion/5
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/calificacion/{calificacion}")
    public ResponseEntity<List<Resena>> buscarPorCalificacion(
            @PathVariable Integer calificacion
    ) {

        return ResponseEntity.ok(
                resenaService.buscarPorCalificacion(calificacion)
        );
    }

    // http://localhost:8089/api/v1/resenas/estado/APROBADA
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/estado/{estadoResena}")
    public ResponseEntity<List<Resena>> buscarPorEstado(
            @PathVariable String estadoResena
    ) {

        return ResponseEntity.ok(
                resenaService.buscarPorEstado(estadoResena)
        );
    }
}