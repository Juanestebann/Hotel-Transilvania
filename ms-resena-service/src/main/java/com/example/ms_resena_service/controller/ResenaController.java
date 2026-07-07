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

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

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
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<Resena>> listarResenas() {
        List<Resena> resenas = resenaService.listarResenas();

        resenas.forEach(this::agregarLinksResena);

        return ResponseEntity.ok(resenas);
    }

    @Operation(
            summary = "Buscar reseña por ID",
            description = "Obtiene una reseña específica según su identificador."
    )
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<Resena> buscarPorId(@PathVariable Long id) {
        Resena resena = resenaService.buscarPorId(id);

        agregarLinksResena(resena);

        return ResponseEntity.ok(resena);
    }

    @Operation(
            summary = "Crear reseña",
            description = "Registra una nueva reseña asociada a un cliente, hotel, habitación y reserva."
    )
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<Resena> guardarResena(@Valid @RequestBody Resena resena) {
        Resena resenaGuardada = resenaService.guardarResena(resena);

        agregarLinksResena(resenaGuardada);

        return ResponseEntity.ok(resenaGuardada);
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

        Resena resenaActualizada = resenaService.actualizarResena(id, resena);

        agregarLinksResena(resenaActualizada);

        return ResponseEntity.ok(resenaActualizada);
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
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/cliente/{idCliente}")
    public ResponseEntity<List<Resena>> buscarPorCliente(@PathVariable Long idCliente) {
        List<Resena> resenas = resenaService.buscarPorCliente(idCliente);

        resenas.forEach(this::agregarLinksResena);

        return ResponseEntity.ok(resenas);
    }

    @Operation(
            summary = "Buscar reseñas por hotel",
            description = "Obtiene todas las reseñas asociadas a un hotel específico."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/hotel/{idHotel}")
    public ResponseEntity<List<Resena>> buscarPorHotel(@PathVariable Long idHotel) {
        List<Resena> resenas = resenaService.buscarPorHotel(idHotel);

        resenas.forEach(this::agregarLinksResena);

        return ResponseEntity.ok(resenas);
    }

    @Operation(
            summary = "Buscar reseñas por habitación",
            description = "Obtiene todas las reseñas asociadas a una habitación específica."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/habitacion/{idHabitacion}")
    public ResponseEntity<List<Resena>> buscarPorHabitacion(@PathVariable Long idHabitacion) {
        List<Resena> resenas = resenaService.buscarPorHabitacion(idHabitacion);

        resenas.forEach(this::agregarLinksResena);

        return ResponseEntity.ok(resenas);
    }

    @Operation(
            summary = "Buscar reseñas por reserva",
            description = "Obtiene todas las reseñas asociadas a una reserva específica."
    )
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/reserva/{idReserva}")
    public ResponseEntity<List<Resena>> buscarPorReserva(@PathVariable Long idReserva) {
        List<Resena> resenas = resenaService.buscarPorReserva(idReserva);

        resenas.forEach(this::agregarLinksResena);

        return ResponseEntity.ok(resenas);
    }

    @Operation(
            summary = "Buscar reseñas por calificación",
            description = "Obtiene las reseñas filtradas por calificación."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/calificacion/{calificacion}")
    public ResponseEntity<List<Resena>> buscarPorCalificacion(@PathVariable Integer calificacion) {
        List<Resena> resenas = resenaService.buscarPorCalificacion(calificacion);

        resenas.forEach(this::agregarLinksResena);

        return ResponseEntity.ok(resenas);
    }

    @Operation(
            summary = "Buscar reseñas por estado",
            description = "Obtiene reseñas filtradas por estado. Endpoint disponible solo para administradores."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/estado/{estadoResena}")
    public ResponseEntity<List<Resena>> buscarPorEstado(@PathVariable String estadoResena) {
        List<Resena> resenas = resenaService.buscarPorEstado(estadoResena);

        resenas.forEach(this::agregarLinksResena);

        return ResponseEntity.ok(resenas);
    }

    /**
     * Agrega enlaces HATEOAS a una reseña.
     * Permite navegar entre recursos relacionados de forma dinámica.
     */
    private void agregarLinksResena(Resena resena) {

        // Link al recurso actual (la propia reseña)
        resena.add(linkTo(methodOn(ResenaController.class)
                .buscarPorId(resena.getId())).withSelfRel());

        // Link para listar todas las reseñas
        resena.add(linkTo(methodOn(ResenaController.class)
                .listarResenas()).withRel("todas-las-resenas"));

        // Link para ver las reseñas del mismo cliente
        resena.add(linkTo(methodOn(ResenaController.class)
                .buscarPorCliente(resena.getIdCliente())).withRel("resenas-del-cliente"));

        // Link para ver las reseñas del mismo hotel
        resena.add(linkTo(methodOn(ResenaController.class)
                .buscarPorHotel(resena.getIdHotel())).withRel("resenas-del-hotel"));

        // Link para ver las reseñas de la misma habitación
        resena.add(linkTo(methodOn(ResenaController.class)
                .buscarPorHabitacion(resena.getIdHabitacion())).withRel("resenas-de-la-habitacion"));

        // Link para ver las reseñas de la misma reserva
        resena.add(linkTo(methodOn(ResenaController.class)
                .buscarPorReserva(resena.getIdReserva())).withRel("resenas-de-la-reserva"));

        // Link para buscar reseñas con la misma calificación
        resena.add(linkTo(methodOn(ResenaController.class)
                .buscarPorCalificacion(resena.getCalificacion())).withRel("resenas-por-calificacion"));

        // Link para actualizar esta reseña
        resena.add(linkTo(methodOn(ResenaController.class)
                .actualizarResena(resena.getId(), null)).withRel("actualizar-resena"));

        // Link para eliminar esta reseña
        resena.add(linkTo(methodOn(ResenaController.class)
                .eliminarResena(resena.getId())).withRel("eliminar-resena"));
    }
}
