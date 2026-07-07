package com.example.ms_disponibilidad_service.controller;

import com.example.ms_disponibilidad_service.dto.DisponibilidadEstadoDTO;
import com.example.ms_disponibilidad_service.model.Disponibilidad;
import com.example.ms_disponibilidad_service.service.DisponibilidadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/v1/disponibilidades")
@RequiredArgsConstructor
public class DisponibilidadController {

    private final DisponibilidadService disponibilidadService;

    //http://localhost:8085/api/v1/disponibilidades
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping
    public ResponseEntity<List<Disponibilidad>> findAll() {
        List<Disponibilidad> disponibilidades = disponibilidadService.findAll();
        disponibilidades.forEach(this::agregarLinksDisponibilidad);
        return ResponseEntity.ok(disponibilidades);
    }

    //http://localhost:8085/api/v1/disponibilidades/1
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<Disponibilidad> findById(@PathVariable Long id) {
        Disponibilidad disponibilidad = disponibilidadService.findById(id);
        agregarLinksDisponibilidad(disponibilidad);
        return ResponseEntity.ok(disponibilidad);
    }

    //http://localhost:8085/api/v1/disponibilidades/habitacion/1/fecha/2026-05-21
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/habitacion/{idHabitacion}/fecha/{fecha}")
    public ResponseEntity<Disponibilidad> findByIdHabitacionAndFecha(
            @PathVariable Long idHabitacion,
            @PathVariable LocalDate fecha) {

        Disponibilidad disponibilidad =
                disponibilidadService.findByIdHabitacionAndFecha(idHabitacion, fecha);
        agregarLinksDisponibilidad(disponibilidad);
        return ResponseEntity.ok(disponibilidad);
    }

    //http://localhost:8085/api/v1/disponibilidades
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Disponibilidad> guardar(
            @Valid @RequestBody Disponibilidad disponibilidad) {

        Disponibilidad nuevaDisponibilidad = disponibilidadService.guardar(disponibilidad);
        agregarLinksDisponibilidad(nuevaDisponibilidad);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(nuevaDisponibilidad);
    }

    //http://localhost:8085/api/v1/disponibilidades/18
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Disponibilidad> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody Disponibilidad disponibilidad) {

        Disponibilidad disponibilidadActualizada =
                disponibilidadService.actualizar(id, disponibilidad);
        agregarLinksDisponibilidad(disponibilidadActualizada);
        return ResponseEntity.ok(disponibilidadActualizada);
    }

    @PreAuthorize("hasRole('SERVICE') and authentication.name == 'ms-reserva-service'")
    @PutMapping("/internal/{id}")
    public ResponseEntity<Disponibilidad> actualizarEstadoInterno(
            @PathVariable Long id,
            @Valid @RequestBody DisponibilidadEstadoDTO solicitud) {

        Disponibilidad disponibilidadActualizada =
                disponibilidadService.actualizarEstadoInterno(id, solicitud.estado());

        return ResponseEntity.ok(disponibilidadActualizada);
    }

    //http://localhost:8085/api/v1/disponibilidades/17
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {

        disponibilidadService.eliminar(id);

        return ResponseEntity.noContent().build();
    }

    private void agregarLinksDisponibilidad(Disponibilidad disponibilidad) {
        disponibilidad.add(linkTo(methodOn(DisponibilidadController.class)
                .findById(disponibilidad.getId())).withSelfRel());
        disponibilidad.add(linkTo(methodOn(DisponibilidadController.class)
                .findAll()).withRel("listar-todos"));
        disponibilidad.add(linkTo(methodOn(DisponibilidadController.class)
                .guardar(null)).withRel("crear"));
        disponibilidad.add(linkTo(methodOn(DisponibilidadController.class)
                .actualizar(disponibilidad.getId(), null)).withRel("actualizar"));
        disponibilidad.add(linkTo(methodOn(DisponibilidadController.class)
                .eliminar(disponibilidad.getId())).withRel("eliminar"));
        disponibilidad.add(linkTo(methodOn(DisponibilidadController.class)
                .findByIdHabitacionAndFecha(
                        disponibilidad.getIdHabitacion(),
                        disponibilidad.getFecha()
                )).withRel("buscar-por-habitacion-fecha"));
    }
}
