package com.example.ms_resena_service.controller;

import com.example.ms_resena_service.model.Resena;
import com.example.ms_resena_service.service.ResenaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Reseñas",
        description = "Operaciones relacionadas con la gestión de reseñas"
)
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/resenas")
public class ResenaController {

    private final ResenaService resenaService;

    public ResenaController(ResenaService resenaService) {
        this.resenaService = resenaService;
    }

    @Operation(
            summary = "Listar reseñas",
            description = "Obtiene todas las reseñas registradas en el sistema."
    )
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping
    public ResponseEntity<List<Resena>> listarResenas() {
        return ResponseEntity.ok(resenaService.listarResenas());
    }

    @Operation(
            summary = "Buscar reseña por ID",
            description = "Obtiene una reseña específica según su identificador."
    )
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<Resena> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(resenaService.buscarPorId(id));
    }

    @Operation(
            summary = "Crear reseña",
            description = "Registra una nueva reseña asociada a un cliente, hotel, habitación y reserva."
    )
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<Resena> guardarResena(@Valid @RequestBody Resena resena) {
        return ResponseEntity.ok(resenaService.guardarResena(resena));
    }

    @Operation(
            summary = "Actualizar reseña",
            description = "Modifica los datos de una reseña existente."
    )
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Resena> actualizarResena(
            @PathVariable Long id,
            @Valid @RequestBody Resena resena) {

        return ResponseEntity.ok(resenaService.actualizarResena(id, resena));
    }

    @Operation(
            summary = "Eliminar reseña",
            description = "Elimina una reseña según su identificador."
    )
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminarResena(@PathVariable Long id) {
        resenaService.eliminarResena(id);
        return ResponseEntity.ok("Reseña eliminada correctamente");
    }

    @Operation(
            summary = "Buscar reseñas por cliente",
            description = "Obtiene todas las reseñas asociadas a un cliente específico."
    )
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/cliente/{idCliente}")
    public ResponseEntity<List<Resena>> buscarPorCliente(@PathVariable Long idCliente) {
        return ResponseEntity.ok(resenaService.buscarPorCliente(idCliente));
    }

    @Operation(
            summary = "Buscar reseñas por hotel",
            description = "Obtiene todas las reseñas asociadas a un hotel específico."
    )
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/hotel/{idHotel}")
    public ResponseEntity<List<Resena>> buscarPorHotel(@PathVariable Long idHotel) {
        return ResponseEntity.ok(resenaService.buscarPorHotel(idHotel));
    }

    @Operation(
            summary = "Buscar reseñas por habitación",
            description = "Obtiene todas las reseñas asociadas a una habitación específica."
    )
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/habitacion/{idHabitacion}")
    public ResponseEntity<List<Resena>> buscarPorHabitacion(@PathVariable Long idHabitacion) {
        return ResponseEntity.ok(resenaService.buscarPorHabitacion(idHabitacion));
    }

    @Operation(
            summary = "Buscar reseñas por reserva",
            description = "Obtiene todas las reseñas asociadas a una reserva específica."
    )
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/reserva/{idReserva}")
    public ResponseEntity<List<Resena>> buscarPorReserva(@PathVariable Long idReserva) {
        return ResponseEntity.ok(resenaService.buscarPorReserva(idReserva));
    }

    @Operation(
            summary = "Buscar reseñas por calificación",
            description = "Obtiene las reseñas filtradas por calificación."
    )
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/calificacion/{calificacion}")
    public ResponseEntity<List<Resena>> buscarPorCalificacion(@PathVariable Integer calificacion) {
        return ResponseEntity.ok(resenaService.buscarPorCalificacion(calificacion));
    }

    @Operation(
            summary = "Buscar reseñas por estado",
            description = "Obtiene reseñas filtradas por estado. Endpoint disponible solo para administradores."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/estado/{estadoResena}")
    public ResponseEntity<List<Resena>> buscarPorEstado(@PathVariable String estadoResena) {
        return ResponseEntity.ok(resenaService.buscarPorEstado(estadoResena));
    }
}