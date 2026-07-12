package com.example.ms_reserva_service.controller;

import com.example.ms_reserva_service.model.Reserva;
import com.example.ms_reserva_service.service.ReservaService;
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
        name = "Reservas",
        description = "Operaciones protegidas para crear, consultar y administrar reservas"
)
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/reservas")
@RequiredArgsConstructor
public class ReservaController {

    private final ReservaService reservaService;

    @Operation(
            summary = "Listar reservas",
            description = "Lista todas las reservas o filtra por idCliente, idUsuario, idHotel, idHabitacion o estadoReserva. Requiere rol ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservas listadas correctamente"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuario sin permisos de ADMIN", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<Reserva>> findAll(
            @RequestParam(required = false) Long idCliente,
            @RequestParam(required = false) Long idUsuario,
            @RequestParam(required = false) Long idHotel,
            @RequestParam(required = false) Long idHabitacion,
            @RequestParam(required = false) String estadoReserva) {

        List<Reserva> reservas;

        if (idCliente != null) {
            reservas = reservaService.findByIdCliente(idCliente);
        } else if (idUsuario != null) {
            reservas = reservaService.findByIdUsuario(idUsuario);
        } else if (idHotel != null) {
            reservas = reservaService.findByIdHotel(idHotel);
        } else if (idHabitacion != null) {
            reservas = reservaService.findByIdHabitacion(idHabitacion);
        } else if (estadoReserva != null) {
            reservas = reservaService.findByEstadoReserva(estadoReserva);
        } else {
            reservas = reservaService.findAll();
        }

        reservas.forEach(this::agregarLinksReserva);

        return ResponseEntity.status(HttpStatus.OK).body(reservas);
    }

    @Operation(
            summary = "Buscar reserva por ID",
            description = "Obtiene una reserva por ID. Requiere rol USER o ADMIN; USER solo puede acceder a reservas propias."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reserva encontrada"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuario sin permisos sobre la reserva", content = @Content),
            @ApiResponse(responseCode = "404", description = "Reserva no encontrada", content = @Content),
            @ApiResponse(responseCode = "503", description = "Auth-service no disponible al validar usuario actual", content = @Content),
            @ApiResponse(responseCode = "504", description = "Timeout consultando Auth-service", content = @Content)
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<Reserva> findById(@PathVariable Long id) {
        Reserva reserva = reservaService.findById(id);
        agregarLinksReserva(reserva);

        return ResponseEntity.status(HttpStatus.OK).body(reserva);
    }

    @Operation(
            summary = "Crear reserva",
            description = "Crea una reserva y valida cliente, usuario, hotel, habitación y disponibilidad mediante microservicios externos. USER debe crear reservas propias en estado PENDIENTE; ADMIN puede crear para otros usuarios.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = Reserva.class),
                            examples = @ExampleObject(
                                    name = "Reserva",
                                    value = """
                                            {
                                              "idCliente": 1,
                                              "idUsuario": 1,
                                              "idHotel": 1,
                                              "idHabitacion": 1,
                                              "fechaInicio": "2026-08-01",
                                              "fechaFin": "2026-08-05",
                                              "cantidadPersonas": 2,
                                              "estadoReserva": "PENDIENTE",
                                              "fechaCreacion": "2026-07-12T10:00:00"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Reserva creada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos, fechas inválidas, capacidad excedida, habitación no disponible o estado inválido", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuario sin permisos para crear la reserva solicitada", content = @Content),
            @ApiResponse(responseCode = "404", description = "Cliente, usuario, hotel, habitación o disponibilidad no encontrada", content = @Content),
            @ApiResponse(responseCode = "503", description = "Servicio remoto requerido no disponible", content = @Content),
            @ApiResponse(responseCode = "504", description = "Timeout consultando servicio remoto requerido", content = @Content)
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<Reserva> guardarReserva(@Valid @RequestBody Reserva reserva) {
        Reserva reservaGuardada = reservaService.guardar(reserva);
        agregarLinksReserva(reservaGuardada);

        return ResponseEntity.status(HttpStatus.CREATED).body(reservaGuardada);
    }

    @Operation(
            summary = "Actualizar reserva",
            description = "Actualiza una reserva existente, valida relaciones remotas y aplica reglas de transición de estado. Requiere rol ADMIN.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = Reserva.class),
                            examples = @ExampleObject(
                                    name = "Reserva actualizada",
                                    value = """
                                            {
                                              "idCliente": 1,
                                              "idUsuario": 1,
                                              "idHotel": 1,
                                              "idHabitacion": 2,
                                              "fechaInicio": "2026-08-10",
                                              "fechaFin": "2026-08-12",
                                              "cantidadPersonas": 2,
                                              "estadoReserva": "CONFIRMADA",
                                              "fechaCreacion": "2026-07-12T10:00:00"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reserva actualizada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o transición de reserva no permitida", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuario sin permisos de ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Reserva o recurso relacionado no encontrado", content = @Content),
            @ApiResponse(responseCode = "503", description = "Servicio remoto requerido no disponible", content = @Content),
            @ApiResponse(responseCode = "504", description = "Timeout consultando servicio remoto requerido", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Reserva> actualizarReserva(
            @PathVariable Long id,
            @Valid @RequestBody Reserva reserva) {

        Reserva reservaActualizada = reservaService.actualizar(id, reserva);
        agregarLinksReserva(reservaActualizada);

        return ResponseEntity.status(HttpStatus.OK).body(reservaActualizada);
    }

    @Operation(
            summary = "Eliminar reserva",
            description = "Elimina una reserva y libera disponibilidad asociada cuando corresponde. Requiere rol ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Reserva eliminada correctamente", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuario sin permisos de ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Reserva no encontrada", content = @Content),
            @ApiResponse(responseCode = "503", description = "Servicio remoto requerido no disponible al liberar disponibilidad", content = @Content),
            @ApiResponse(responseCode = "504", description = "Timeout liberando disponibilidad", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarReserva(@PathVariable Long id) {
        reservaService.eliminar(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(
            summary = "Cambiar estado de reserva",
            description = "Cambia estadoReserva aplicando transiciones permitidas. USER solo puede cancelar reservas propias; ADMIN puede ejecutar cambios administrativos."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado de reserva actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Estado inválido o transición de reserva no permitida", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuario sin permisos sobre la reserva o cambio solicitado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Reserva o disponibilidad no encontrada", content = @Content),
            @ApiResponse(responseCode = "503", description = "Servicio remoto requerido no disponible", content = @Content),
            @ApiResponse(responseCode = "504", description = "Timeout consultando servicio remoto requerido", content = @Content)
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PutMapping("/{id}/estado")
    public ResponseEntity<Reserva> cambiarEstado(
            @PathVariable Long id,
            @RequestParam String estadoReserva) {

        Reserva reservaActualizada = reservaService.cambiarEstado(id, estadoReserva);
        agregarLinksReserva(reservaActualizada);

        return ResponseEntity.status(HttpStatus.OK).body(reservaActualizada);
    }

    @Operation(
            summary = "Cambiar estado desde pago",
            description = "Endpoint interno usado por ms-pago-service para confirmar o cancelar reservas según resultado de pago. Requiere token SERVICE con subject autorizado ms-pago-service."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado correctamente desde pago"),
            @ApiResponse(responseCode = "400", description = "Estado interno inválido o transición no permitida", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Token SERVICE no autorizado o subject distinto de ms-pago-service", content = @Content),
            @ApiResponse(responseCode = "404", description = "Reserva o disponibilidad no encontrada", content = @Content),
            @ApiResponse(responseCode = "503", description = "ms-disponibilidad-service no disponible", content = @Content),
            @ApiResponse(responseCode = "504", description = "Timeout consultando ms-disponibilidad-service", content = @Content)
    })
    @PreAuthorize("hasRole('SERVICE') and authentication.name == 'ms-pago-service'")
    @PutMapping("/internal/{id}/estado")
    public ResponseEntity<Reserva> cambiarEstadoDesdePago(
            @PathVariable Long id,
            @RequestParam String estadoReserva) {

        Reserva reservaActualizada = reservaService.cambiarEstadoInterno(id, estadoReserva);
        return ResponseEntity.ok(reservaActualizada);
    }

    private void agregarLinksReserva(Reserva reserva) {

        reserva.removeLinks();

        reserva.add(linkTo(methodOn(ReservaController.class)
                .findById(reserva.getId())).withSelfRel());

        reserva.add(linkTo(methodOn(ReservaController.class)
                .findAll(null, null, null, null, null)).withRel("todas-las-reservas"));

        reserva.add(linkTo(methodOn(ReservaController.class)
                .guardarReserva(null)).withRel("crear-reserva"));

        reserva.add(linkTo(methodOn(ReservaController.class)
                .actualizarReserva(reserva.getId(), null)).withRel("actualizar-reserva"));

        reserva.add(linkTo(methodOn(ReservaController.class)
                .cambiarEstado(reserva.getId(), null)).withRel("cambiar-estado"));

        reserva.add(linkTo(methodOn(ReservaController.class)
                .eliminarReserva(reserva.getId())).withRel("eliminar-reserva"));
    }
}
