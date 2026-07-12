package com.example.ms_pago_service.controller;

import com.example.ms_pago_service.model.Pago;
import com.example.ms_pago_service.service.PagoService;
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
        name = "Pagos",
        description = "Operaciones protegidas para registrar, consultar y administrar pagos de reservas"
)
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;

    @Operation(
            summary = "Listar pagos",
            description = "Obtiene todos los pagos registrados. Requiere rol ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pagos listados correctamente"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuario sin permisos de ADMIN", content = @Content)
    })
    // http://localhost:8087/api/v1/pagos
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<Pago>> findAll() {
        List<Pago> pagos = pagoService.findAll();

        pagos.forEach(this::agregarLinksPago);

        return ResponseEntity.ok(pagos);
    }

    @Operation(
            summary = "Buscar pago por ID",
            description = "Obtiene un pago por ID. Requiere rol USER o ADMIN; USER solo puede acceder a pagos propios."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pago encontrado"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuario sin permisos sobre el pago", content = @Content),
            @ApiResponse(responseCode = "404", description = "Pago no encontrado", content = @Content),
            @ApiResponse(responseCode = "503", description = "Auth-service no disponible al validar usuario actual", content = @Content),
            @ApiResponse(responseCode = "504", description = "Timeout consultando Auth-service", content = @Content)
    })
    // http://localhost:8087/api/v1/pagos/1
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<Pago> findById(@PathVariable Long id) {
        Pago pago = pagoService.findById(id);

        agregarLinksPago(pago);

        return ResponseEntity.ok(pago);
    }

    @Operation(
            summary = "Crear pago",
            description = "Registra un pago, valida reserva y usuario con microservicios externos, evita doble pago aprobado y puede actualizar estado de reserva desde ms-pago-service. Requiere rol USER o ADMIN.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = Pago.class),
                            examples = @ExampleObject(
                                    name = "Pago",
                                    value = """
                                            {
                                              "reservaId": 1,
                                              "idUsuario": 1,
                                              "monto": 250000.00,
                                              "metodoPago": "TARJETA",
                                              "estadoPago": "APROBADO",
                                              "fechaPago": "2026-07-12T10:30:00"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pago creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos, monto inválido, estado inválido o reserva ya pagada", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuario sin permisos para pagar la reserva solicitada", content = @Content),
            @ApiResponse(responseCode = "404", description = "Reserva o usuario relacionado no encontrado", content = @Content),
            @ApiResponse(responseCode = "503", description = "Servicio remoto requerido no disponible", content = @Content),
            @ApiResponse(responseCode = "504", description = "Timeout consultando servicio remoto requerido", content = @Content)
    })
    // http://localhost:8087/api/v1/pagos
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<Pago> save(@Valid @RequestBody Pago pago) {

        Pago nuevoPago = pagoService.save(pago);

        agregarLinksPago(nuevoPago);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(nuevoPago);
    }

    @Operation(
            summary = "Actualizar pago",
            description = "Actualiza un pago existente, revalida reserva y usuario relacionados, y puede sincronizar el estado de reserva. Requiere rol ADMIN.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = Pago.class),
                            examples = @ExampleObject(
                                    name = "Pago actualizado",
                                    value = """
                                            {
                                              "reservaId": 1,
                                              "idUsuario": 1,
                                              "monto": 250000.00,
                                              "metodoPago": "TRANSFERENCIA",
                                              "estadoPago": "PENDIENTE",
                                              "fechaPago": "2026-07-12T11:00:00"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pago actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos, monto inválido, estado inválido o reserva ya pagada", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuario sin permisos de ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Pago, reserva o usuario relacionado no encontrado", content = @Content),
            @ApiResponse(responseCode = "503", description = "Servicio remoto requerido no disponible", content = @Content),
            @ApiResponse(responseCode = "504", description = "Timeout consultando servicio remoto requerido", content = @Content)
    })
    // http://localhost:8087/api/v1/pagos/1
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Pago> update(
            @PathVariable Long id,
            @Valid @RequestBody Pago pago) {

        Pago pagoActualizado = pagoService.update(id, pago);

        agregarLinksPago(pagoActualizado);

        return ResponseEntity.ok(pagoActualizado);
    }

    @Operation(
            summary = "Eliminar pago",
            description = "Elimina un pago por su identificador. Requiere rol ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Pago eliminado correctamente", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuario sin permisos de ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Pago no encontrado", content = @Content)
    })
    // http://localhost:8087/api/v1/pagos/1
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {

        pagoService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Buscar pagos por reserva",
            description = "Lista pagos asociados a una reserva. Requiere rol USER o ADMIN; USER solo puede consultar reservas propias."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pagos de la reserva listados correctamente"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuario sin permisos sobre la reserva", content = @Content),
            @ApiResponse(responseCode = "404", description = "Reserva no encontrada", content = @Content),
            @ApiResponse(responseCode = "503", description = "Servicio remoto requerido no disponible", content = @Content),
            @ApiResponse(responseCode = "504", description = "Timeout consultando servicio remoto requerido", content = @Content)
    })
    // http://localhost:8087/api/v1/pagos/reserva/1
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/reserva/{reservaId}")
    public ResponseEntity<List<Pago>> findByReservaId(@PathVariable Long reservaId) {

        List<Pago> pagos = pagoService.findByReservaId(reservaId);

        pagos.forEach(this::agregarLinksPago);

        return ResponseEntity.ok(pagos);
    }

    @Operation(
            summary = "Buscar pagos por estado",
            description = "Lista pagos filtrados por estadoPago. Valores esperados: PENDIENTE, APROBADO, RECHAZADO, REEMBOLSADO o ANULADO. Requiere rol ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pagos filtrados correctamente"),
            @ApiResponse(responseCode = "400", description = "Estado de pago inválido", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuario sin permisos de ADMIN", content = @Content)
    })
    // http://localhost:8087/api/v1/pagos/estado/APROBADO
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/estado/{estadoPago}")
    public ResponseEntity<List<Pago>> findByEstadoPago(@PathVariable String estadoPago) {

        List<Pago> pagos = pagoService.findByEstadoPago(estadoPago);

        pagos.forEach(this::agregarLinksPago);

        return ResponseEntity.ok(pagos);
    }

    /**
     * Agrega enlaces HATEOAS a un pago.
     * Permite navegar entre recursos relacionados de forma dinámica.
     */
    private void agregarLinksPago(Pago pago) {

        // Link al recurso actual (el propio pago)
        pago.add(linkTo(methodOn(PagoController.class)
                .findById(pago.getIdPago())).withSelfRel());

        // Link para listar todos los pagos
        pago.add(linkTo(methodOn(PagoController.class)
                .findAll()).withRel("todos-los-pagos"));

        // Link para ver pagos asociados a la misma reserva
        pago.add(linkTo(methodOn(PagoController.class)
                .findByReservaId(pago.getReservaId())).withRel("pagos-de-la-reserva"));

        // Link para ver pagos con el mismo estado
        pago.add(linkTo(methodOn(PagoController.class)
                .findByEstadoPago(pago.getEstadoPago())).withRel("pagos-por-estado"));

        // Link para actualizar este pago
        pago.add(linkTo(methodOn(PagoController.class)
                .update(pago.getIdPago(), null)).withRel("actualizar-pago"));

        // Link para eliminar este pago
        pago.add(linkTo(methodOn(PagoController.class)
                .delete(pago.getIdPago())).withRel("eliminar-pago"));
    }
}
