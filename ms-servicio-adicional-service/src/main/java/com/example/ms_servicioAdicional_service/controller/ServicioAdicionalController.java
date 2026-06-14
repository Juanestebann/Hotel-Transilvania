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

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

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
            List<ServicioAdicionalModel> servicios = servicioAdicionalService.findByIdHotel(idHotel);
            servicios.forEach(this::agregarLinksServicio);
            return ResponseEntity.status(HttpStatus.OK).body(servicios);
        }

        if (idReserva != null) {
            List<ServicioAdicionalModel> servicios = servicioAdicionalService.findByIdReserva(idReserva);
            servicios.forEach(this::agregarLinksServicio);
            return ResponseEntity.status(HttpStatus.OK).body(servicios);
        }

        if (estado != null) {
            List<ServicioAdicionalModel> servicios = servicioAdicionalService.findByEstado(estado);
            servicios.forEach(this::agregarLinksServicio);
            return ResponseEntity.status(HttpStatus.OK).body(servicios);
        }

        if (nombre != null) {
            List<ServicioAdicionalModel> servicios = servicioAdicionalService.findByNombre(nombre);
            servicios.forEach(this::agregarLinksServicio);
            return ResponseEntity.status(HttpStatus.OK).body(servicios);
        }

        List<ServicioAdicionalModel> servicios = servicioAdicionalService.findAll();
        servicios.forEach(this::agregarLinksServicio);

        return ResponseEntity.status(HttpStatus.OK).body(servicios);
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

        agregarLinksServicio(servicio);

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

        agregarLinksServicio(nuevoServicio);

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

        agregarLinksServicio(servicioActualizado);

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

    /**
     * Agrega enlaces HATEOAS a un servicio adicional.
     * Permite navegar hacia recursos y acciones relacionadas desde la respuesta.
     */
    private void agregarLinksServicio(ServicioAdicionalModel servicio) {

        // Link al recurso actual
        servicio.add(linkTo(methodOn(ServicioAdicionalController.class)
                .findById(servicio.getId())).withSelfRel());

        // Link para listar todos los servicios adicionales
        servicio.add(linkTo(methodOn(ServicioAdicionalController.class)
                .findAll(null, null, null, null)).withRel("todos-los-servicios"));

        // Link para buscar servicios del mismo hotel
        servicio.add(linkTo(methodOn(ServicioAdicionalController.class)
                .findAll(servicio.getIdHotel(), null, null, null)).withRel("servicios-del-hotel"));

        // Link para buscar servicios de la misma reserva
        servicio.add(linkTo(methodOn(ServicioAdicionalController.class)
                .findAll(null, servicio.getIdReserva(), null, null)).withRel("servicios-de-la-reserva"));

        // Link para buscar servicios con el mismo estado
        servicio.add(linkTo(methodOn(ServicioAdicionalController.class)
                .findAll(null, null, servicio.getEstado(), null)).withRel("servicios-por-estado"));

        // Link para buscar servicios con el mismo nombre
        servicio.add(linkTo(methodOn(ServicioAdicionalController.class)
                .findAll(null, null, null, servicio.getNombre())).withRel("servicios-por-nombre"));

        // Link para actualizar este servicio adicional
        servicio.add(linkTo(methodOn(ServicioAdicionalController.class)
                .actualizarServicio(servicio.getId(), null)).withRel("actualizar-servicio"));

        // Link para eliminar este servicio adicional
        servicio.add(linkTo(methodOn(ServicioAdicionalController.class)
                .eliminarServicio(servicio.getId())).withRel("eliminar-servicio"));
    }
}