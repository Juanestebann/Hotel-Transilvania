package com.example.ms_notificacion_service.controller;

import com.example.ms_notificacion_service.model.Notificacion;
import com.example.ms_notificacion_service.service.NotificacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/v1/notificaciones")
@RequiredArgsConstructor
@Tag(
        name = "Notificaciones",
        description = "Operaciones relacionadas con la gestión de notificaciones"
)
@SecurityRequirement(name = "bearerAuth")
public class NotificacionController {

    private final NotificacionService notificacionService;

    // http://localhost:8088/api/v1/notificaciones
    // http://localhost:8088/api/v1/notificaciones?idCliente=1
    // http://localhost:8088/api/v1/notificaciones?idUsuario=1
    // http://localhost:8088/api/v1/notificaciones?idReserva=1
    @Operation(
            summary = "Listar notificaciones",
            description = "Obtiene todas las notificaciones o permite filtrarlas por cliente, usuario o reserva."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<?> findAll(
            @RequestParam(required = false) Long idCliente,
            @RequestParam(required = false) Long idUsuario,
            @RequestParam(required = false) Long idReserva
    ) {

        if (idCliente != null) {
            List<Notificacion> notificaciones = notificacionService.findByIdCliente(idCliente);
            notificaciones.forEach(this::agregarLinksNotificacion);
            return ResponseEntity.status(HttpStatus.OK).body(notificaciones);
        }

        if (idUsuario != null) {
            List<Notificacion> notificaciones = notificacionService.findByIdUsuario(idUsuario);
            notificaciones.forEach(this::agregarLinksNotificacion);
            return ResponseEntity.status(HttpStatus.OK).body(notificaciones);
        }

        if (idReserva != null) {
            List<Notificacion> notificaciones = notificacionService.findByIdReserva(idReserva);
            notificaciones.forEach(this::agregarLinksNotificacion);
            return ResponseEntity.status(HttpStatus.OK).body(notificaciones);
        }

        List<Notificacion> notificaciones = notificacionService.findAll();
        notificaciones.forEach(this::agregarLinksNotificacion);

        return ResponseEntity.status(HttpStatus.OK)
                .body(notificaciones);
    }

    // http://localhost:8088/api/v1/notificaciones/1
    @Operation(
            summary = "Buscar notificación por ID",
            description = "Obtiene una notificación específica según su identificador."
    )
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<Notificacion> findById(@PathVariable Long id) {
        Notificacion notificacion = notificacionService.findById(id);

        agregarLinksNotificacion(notificacion);

        return ResponseEntity.status(HttpStatus.OK)
                .body(notificacion);
    }

    // http://localhost:8088/api/v1/notificaciones
    @Operation(
            summary = "Crear notificación",
            description = "Registra una nueva notificación. Disponible solo para administradores."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Notificacion> guardar(
            @Valid @RequestBody Notificacion notificacion
    ) {
        Notificacion nuevaNotificacion = notificacionService.guardar(notificacion);

        agregarLinksNotificacion(nuevaNotificacion);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(nuevaNotificacion);
    }

    // http://localhost:8088/api/v1/notificaciones/1
    @Operation(
            summary = "Actualizar notificación",
            description = "Actualiza una notificación existente. Disponible solo para administradores."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Notificacion> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody Notificacion notificacion
    ) {

        Notificacion notificacionActualizada =
                notificacionService.actualizar(id, notificacion);

        agregarLinksNotificacion(notificacionActualizada);

        return ResponseEntity.status(HttpStatus.OK)
                .body(notificacionActualizada);
    }

    // http://localhost:8088/api/v1/notificaciones/1
    @Operation(
            summary = "Eliminar notificación",
            description = "Elimina una notificación según su identificador. Disponible solo para administradores."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {

        notificacionService.eliminar(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * Agrega enlaces HATEOAS a una notificación.
     * Permite navegar hacia recursos y acciones relacionadas desde la respuesta.
     */
    private void agregarLinksNotificacion(Notificacion notificacion) {

        // Link al recurso actual
        notificacion.add(linkTo(methodOn(NotificacionController.class)
                .findById(notificacion.getId())).withSelfRel());

        // Link para listar todas las notificaciones
        notificacion.add(linkTo(methodOn(NotificacionController.class)
                .findAll(null, null, null)).withRel("todas-las-notificaciones"));

        // Link para buscar notificaciones del mismo cliente
        notificacion.add(linkTo(methodOn(NotificacionController.class)
                .findAll(notificacion.getIdCliente(), null, null)).withRel("notificaciones-del-cliente"));

        // Link para buscar notificaciones del mismo usuario
        notificacion.add(linkTo(methodOn(NotificacionController.class)
                .findAll(null, notificacion.getIdUsuario(), null)).withRel("notificaciones-del-usuario"));

        // Link para buscar notificaciones de la misma reserva
        notificacion.add(linkTo(methodOn(NotificacionController.class)
                .findAll(null, null, notificacion.getIdReserva())).withRel("notificaciones-de-la-reserva"));

        // Link para actualizar esta notificación
        notificacion.add(linkTo(methodOn(NotificacionController.class)
                .actualizar(notificacion.getId(), null)).withRel("actualizar-notificacion"));

        // Link para eliminar esta notificación
        notificacion.add(linkTo(methodOn(NotificacionController.class)
                .eliminar(notificacion.getId())).withRel("eliminar-notificacion"));
    }
}