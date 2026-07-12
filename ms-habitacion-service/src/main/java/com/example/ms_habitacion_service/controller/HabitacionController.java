package com.example.ms_habitacion_service.controller;

import com.example.ms_habitacion_service.model.Habitacion;
import com.example.ms_habitacion_service.service.HabitacionService;
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

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Tag(
        name = "Habitaciones",
        description = "Operaciones de consulta pública y administración protegida de habitaciones"
)
@RestController
@RequestMapping("/api/v1/habitaciones")
@RequiredArgsConstructor
public class HabitacionController {

    private final HabitacionService habitacionService;

    @Operation(
            summary = "Listar habitaciones",
            description = "Obtiene todas las habitaciones registradas. Endpoint público de consulta."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Habitaciones listadas correctamente")
    })
    @GetMapping
    public ResponseEntity<List<Habitacion>> findAll() {
        List<Habitacion> habitaciones = habitacionService.findAll();

        habitaciones.forEach(this::agregarLinksHabitacion);

        return ResponseEntity.status(HttpStatus.OK).body(habitaciones);
    }

    @Operation(
            summary = "Buscar habitación por ID",
            description = "Obtiene una habitación específica por su identificador. Endpoint público de consulta."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Habitación encontrada"),
            @ApiResponse(responseCode = "404", description = "Habitación no encontrada", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<Habitacion> findById(@PathVariable Long id) {
        Habitacion habitacion = habitacionService.findById(id);

        agregarLinksHabitacion(habitacion);

        return ResponseEntity.status(HttpStatus.OK).body(habitacion);
    }

    @Operation(
            summary = "Buscar habitaciones por estado",
            description = "Lista habitaciones por estado. Valores esperados: DISPONIBLE, OCUPADA o MANTENIMIENTO."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Habitaciones filtradas correctamente"),
            @ApiResponse(responseCode = "400", description = "Estado de habitación inválido", content = @Content)
    })
    @GetMapping("/estado/{estadoHabitacion}")
    public ResponseEntity<List<Habitacion>> findByEstadoHabitacion(
            @PathVariable String estadoHabitacion) {

        List<Habitacion> habitaciones = habitacionService.findByEstadoHabitacion(estadoHabitacion);

        habitaciones.forEach(this::agregarLinksHabitacion);

        return ResponseEntity.ok(habitaciones);
    }

    @Operation(
            summary = "Buscar habitaciones por capacidad mínima",
            description = "Lista habitaciones cuya capacidad sea mayor o igual a la capacidad solicitada."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Habitaciones filtradas correctamente")
    })
    @GetMapping("/capacidad/{capacidad}")
    public ResponseEntity<List<Habitacion>> findByCapacidadMinima(
            @PathVariable Integer capacidad) {

        List<Habitacion> habitaciones = habitacionService.findByCapacidadMinima(capacidad);

        habitaciones.forEach(this::agregarLinksHabitacion);

        return ResponseEntity.ok(habitaciones);
    }

    @Operation(
            summary = "Buscar habitaciones por hotel",
            description = "Lista habitaciones asociadas a un hotel por idHotel."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Habitaciones del hotel listadas correctamente")
    })
    @GetMapping("/hotel/{idHotel}")
    public ResponseEntity<List<Habitacion>> findByIdHotel(
            @PathVariable Long idHotel) {

        List<Habitacion> habitaciones = habitacionService.findByIdHotel(idHotel);

        habitaciones.forEach(this::agregarLinksHabitacion);

        return ResponseEntity.ok(habitaciones);
    }

    @Operation(
            summary = "Crear habitación",
            description = "Registra una habitación y valida que el hotel exista mediante ms-hotel-service. Requiere rol ADMIN.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = Habitacion.class),
                            examples = @ExampleObject(
                                    name = "Habitación",
                                    value = """
                                            {
                                              "numeroHabitacion": "501",
                                              "tipoHabitacion": "SUITE",
                                              "precioBase": 120000.0,
                                              "capacidad": 2,
                                              "estadoHabitacion": "DISPONIBLE",
                                              "idHotel": 1
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Habitación creada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o estado de habitación inválido", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuario sin permisos de ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Hotel relacionado no encontrado", content = @Content),
            @ApiResponse(responseCode = "503", description = "ms-hotel-service no disponible", content = @Content),
            @ApiResponse(responseCode = "504", description = "Timeout consultando ms-hotel-service", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Habitacion> guardarHabitacion(
            @Valid @RequestBody Habitacion habitacion) {

        Habitacion habitacionGuardada = habitacionService.guardar(habitacion);

        agregarLinksHabitacion(habitacionGuardada);

        return ResponseEntity.status(HttpStatus.CREATED).body(habitacionGuardada);
    }

    @Operation(
            summary = "Actualizar habitación",
            description = "Actualiza una habitación existente y valida el hotel relacionado mediante ms-hotel-service. Requiere rol ADMIN.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = Habitacion.class),
                            examples = @ExampleObject(
                                    name = "Habitación actualizada",
                                    value = """
                                            {
                                              "numeroHabitacion": "501",
                                              "tipoHabitacion": "SUITE PREMIUM",
                                              "precioBase": 145000.0,
                                              "capacidad": 3,
                                              "estadoHabitacion": "DISPONIBLE",
                                              "idHotel": 1
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Habitación actualizada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o estado de habitación inválido", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuario sin permisos de ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Habitación u hotel relacionado no encontrado", content = @Content),
            @ApiResponse(responseCode = "503", description = "ms-hotel-service no disponible", content = @Content),
            @ApiResponse(responseCode = "504", description = "Timeout consultando ms-hotel-service", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Habitacion> actualizarHabitacion(
            @PathVariable Long id,
            @Valid @RequestBody Habitacion habitacion) {

        Habitacion habitacionActualizada = habitacionService.actualizar(id, habitacion);

        agregarLinksHabitacion(habitacionActualizada);

        return ResponseEntity.status(HttpStatus.OK).body(habitacionActualizada);
    }

    @Operation(
            summary = "Eliminar habitación",
            description = "Elimina una habitación por su identificador. Requiere rol ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Habitación eliminada correctamente", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuario sin permisos de ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Habitación no encontrada", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarHabitacion(@PathVariable Long id) {
        habitacionService.eliminar(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    private void agregarLinksHabitacion(Habitacion habitacion) {

        habitacion.removeLinks();

        habitacion.add(linkTo(methodOn(HabitacionController.class)
                .findById(habitacion.getIdHabitacion())).withSelfRel());

        habitacion.add(linkTo(methodOn(HabitacionController.class)
                .findAll()).withRel("todas-las-habitaciones"));

        habitacion.add(linkTo(methodOn(HabitacionController.class)
                .findByEstadoHabitacion(habitacion.getEstadoHabitacion())).withRel("buscar-por-estado"));

        habitacion.add(linkTo(methodOn(HabitacionController.class)
                .findByCapacidadMinima(habitacion.getCapacidad())).withRel("buscar-por-capacidad"));

        habitacion.add(linkTo(methodOn(HabitacionController.class)
                .findByIdHotel(habitacion.getIdHotel())).withRel("buscar-por-hotel"));

        habitacion.add(linkTo(methodOn(HabitacionController.class)
                .guardarHabitacion(null)).withRel("crear-habitacion"));

        habitacion.add(linkTo(methodOn(HabitacionController.class)
                .actualizarHabitacion(habitacion.getIdHabitacion(), null)).withRel("actualizar-habitacion"));

        habitacion.add(linkTo(methodOn(HabitacionController.class)
                .eliminarHabitacion(habitacion.getIdHabitacion())).withRel("eliminar-habitacion"));
    }
}
