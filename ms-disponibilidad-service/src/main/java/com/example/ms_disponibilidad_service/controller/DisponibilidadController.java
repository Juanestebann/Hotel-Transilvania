package com.example.ms_disponibilidad_service.controller;

import com.example.ms_disponibilidad_service.dto.DisponibilidadEstadoDTO;
import com.example.ms_disponibilidad_service.model.Disponibilidad;
import com.example.ms_disponibilidad_service.service.DisponibilidadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Tag(
        name = "Disponibilidad",
        description = "Operaciones protegidas para consultar y administrar disponibilidad de habitaciones"
)
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/disponibilidades")
@RequiredArgsConstructor
public class DisponibilidadController {

    private final DisponibilidadService disponibilidadService;

    @Operation(
            summary = "Listar disponibilidades",
            description = "Obtiene todas las disponibilidades registradas. Requiere rol USER o ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Disponibilidades listadas correctamente"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuario sin permisos para consultar disponibilidades", content = @Content)
    })
    //http://localhost:8085/api/v1/disponibilidades
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping
    public ResponseEntity<List<Disponibilidad>> findAll() {
        List<Disponibilidad> disponibilidades = disponibilidadService.findAll();
        disponibilidades.forEach(this::agregarLinksDisponibilidad);
        return ResponseEntity.ok(disponibilidades);
    }

    @Operation(
            summary = "Buscar disponibilidad por ID",
            description = "Obtiene una disponibilidad por su identificador. Requiere rol USER o ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Disponibilidad encontrada"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuario sin permisos para consultar disponibilidades", content = @Content),
            @ApiResponse(responseCode = "404", description = "Disponibilidad no encontrada", content = @Content)
    })
    //http://localhost:8085/api/v1/disponibilidades/1
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<Disponibilidad> findById(@PathVariable Long id) {
        Disponibilidad disponibilidad = disponibilidadService.findById(id);
        agregarLinksDisponibilidad(disponibilidad);
        return ResponseEntity.ok(disponibilidad);
    }

    @Operation(
            summary = "Buscar disponibilidad por habitación y fecha",
            description = "Obtiene la disponibilidad de una habitación en una fecha específica. Requiere rol USER o ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Disponibilidad encontrada"),
            @ApiResponse(responseCode = "400", description = "Fecha o parámetros inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuario sin permisos para consultar disponibilidades", content = @Content),
            @ApiResponse(responseCode = "404", description = "Disponibilidad no encontrada para habitación y fecha", content = @Content)
    })
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

    @Operation(
            summary = "Buscar disponibilidad interna por habitación y fecha",
            description = "Endpoint interno para ms-reserva-service. Requiere token SERVICE con subject autorizado ms-reserva-service."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Disponibilidad encontrada"),
            @ApiResponse(responseCode = "400", description = "Fecha o parámetros inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Token SERVICE no autorizado o subject distinto de ms-reserva-service", content = @Content),
            @ApiResponse(responseCode = "404", description = "Disponibilidad no encontrada para habitación y fecha", content = @Content)
    })
    @PreAuthorize("hasRole('SERVICE') and authentication.name == 'ms-reserva-service'")
    @GetMapping("/internal/habitacion/{idHabitacion}/fecha/{fecha}")
    public ResponseEntity<Disponibilidad> findByHabitacionAndFechaInterno(
            @PathVariable Long idHabitacion,
            @PathVariable LocalDate fecha) {

        return ResponseEntity.ok(
                disponibilidadService.findByIdHabitacionAndFecha(idHabitacion, fecha)
        );
    }

    @Operation(
            summary = "Crear disponibilidad",
            description = "Registra disponibilidad para una habitación, valida que la habitación exista y evita duplicados por habitación y fecha. Requiere rol ADMIN.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = Disponibilidad.class),
                            examples = @ExampleObject(
                                    name = "Disponibilidad",
                                    value = """
                                            {
                                              "idHabitacion": 1,
                                              "fecha": "2026-08-01",
                                              "estado": "DISPONIBLE"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Disponibilidad creada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos, estado inválido o disponibilidad duplicada", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuario sin permisos de ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Habitación relacionada no encontrada", content = @Content),
            @ApiResponse(responseCode = "503", description = "ms-habitacion-service no disponible", content = @Content),
            @ApiResponse(responseCode = "504", description = "Timeout consultando ms-habitacion-service", content = @Content)
    })
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

    @Operation(
            summary = "Actualizar disponibilidad",
            description = "Actualiza fecha, estado o habitación asociada y valida la habitación remota. Requiere rol ADMIN.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = Disponibilidad.class),
                            examples = @ExampleObject(
                                    name = "Disponibilidad actualizada",
                                    value = """
                                            {
                                              "idHabitacion": 1,
                                              "fecha": "2026-08-02",
                                              "estado": "MANTENIMIENTO"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Disponibilidad actualizada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos, estado inválido o disponibilidad duplicada", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuario sin permisos de ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Disponibilidad u habitación relacionada no encontrada", content = @Content),
            @ApiResponse(responseCode = "503", description = "ms-habitacion-service no disponible", content = @Content),
            @ApiResponse(responseCode = "504", description = "Timeout consultando ms-habitacion-service", content = @Content)
    })
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

    @Operation(
            summary = "Actualizar estado interno de disponibilidad",
            description = "Endpoint interno para que ms-reserva-service cambie estados DISPONIBLE u OCUPADA al confirmar o cancelar reservas. Requiere token SERVICE con subject autorizado ms-reserva-service.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = DisponibilidadEstadoDTO.class),
                            examples = @ExampleObject(
                                    name = "Estado interno",
                                    value = """
                                            {
                                              "estado": "OCUPADA"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Estado inválido o transición no permitida", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Token SERVICE no autorizado o subject distinto de ms-reserva-service", content = @Content),
            @ApiResponse(responseCode = "404", description = "Disponibilidad no encontrada", content = @Content)
    })
    @PreAuthorize("hasRole('SERVICE') and authentication.name == 'ms-reserva-service'")
    @PutMapping("/internal/{id}")
    public ResponseEntity<Disponibilidad> actualizarEstadoInterno(
            @PathVariable Long id,
            @Valid @RequestBody DisponibilidadEstadoDTO solicitud) {

        Disponibilidad disponibilidadActualizada =
                disponibilidadService.actualizarEstadoInterno(id, solicitud.estado());

        return ResponseEntity.ok(disponibilidadActualizada);
    }

    @Operation(
            summary = "Eliminar disponibilidad",
            description = "Elimina una disponibilidad por su identificador. Requiere rol ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Disponibilidad eliminada correctamente", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuario sin permisos de ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Disponibilidad no encontrada", content = @Content)
    })
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
