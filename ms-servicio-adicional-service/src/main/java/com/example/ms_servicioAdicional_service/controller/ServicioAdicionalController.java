package com.example.ms_servicioAdicional_service.controller;

import com.example.ms_servicioAdicional_service.model.ServicioAdicionalModel;
import com.example.ms_servicioAdicional_service.service.ServicioAdicionalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/servicios-adicionales")
@RequiredArgsConstructor
@Tag(
        name = "Servicios Adicionales",
        description = "Operaciones relacionadas con la gestión de servicios adicionales"
)
@SecurityRequirement(name = "bearerAuth")
public class ServicioAdicionalController {

    private final ServicioAdicionalService servicioAdicionalService;

    @Operation(
            summary = "Listar servicios adicionales",
            description = "Obtiene todos los servicios adicionales o permite filtrarlos por hotel, reserva, estado o nombre."
    )
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping
    public ResponseEntity<?> findAll(
            @RequestParam(required = false) Long idHotel,
            @RequestParam(required = false) Long idReserva,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String nombre
    ) {

        if (idHotel != null) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(servicioAdicionalService.findByIdHotel(idHotel));
        }

        if (idReserva != null) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(servicioAdicionalService.findByIdReserva(idReserva));
        }

        if (estado != null) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(servicioAdicionalService.findByEstado(estado));
        }

        if (nombre != null) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(servicioAdicionalService.findByNombre(nombre));
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(servicioAdicionalService.findAll());
    }

    @Operation(
            summary = "Buscar servicio adicional por ID",
            description = "Obtiene un servicio adicional específico según su identificador."
    )
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ServicioAdicionalModel> findById(
            @PathVariable Long id
    ) {

        ServicioAdicionalModel servicio =
                servicioAdicionalService.findById(id);

        return ResponseEntity.status(HttpStatus.OK)
                .body(servicio);
    }

    @Operation(
            summary = "Crear servicio adicional",
            description = "Registra un nuevo servicio adicional. Disponible solo para administradores."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ServicioAdicionalModel> guardarServicio(
            @Valid @RequestBody ServicioAdicionalModel servicioAdicional
    ) {

        ServicioAdicionalModel nuevoServicio =
                servicioAdicionalService.guardar(servicioAdicional);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(nuevoServicio);
    }

    @Operation(
            summary = "Actualizar servicio adicional",
            description = "Actualiza los datos de un servicio adicional existente. Disponible solo para administradores."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ServicioAdicionalModel> actualizarServicio(
            @PathVariable Long id,
            @Valid @RequestBody ServicioAdicionalModel servicioAdicional
    ) {

        ServicioAdicionalModel servicioActualizado =
                servicioAdicionalService.actualizar(id, servicioAdicional);

        return ResponseEntity.status(HttpStatus.OK)
                .body(servicioActualizado);
    }

    @Operation(
            summary = "Eliminar servicio adicional",
            description = "Elimina un servicio adicional según su identificador. Disponible solo para administradores."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarServicio(
            @PathVariable Long id
    ) {

        servicioAdicionalService.eliminar(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .build();
    }
}