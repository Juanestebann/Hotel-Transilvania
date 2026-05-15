package com.example.ms_reserva_service.controller;

import com.example.ms_reserva_service.model.Reserva;
import com.example.ms_reserva_service.service.ReservaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reservas")
@RequiredArgsConstructor
public class ReservaController {

    private final ReservaService reservaService;

    // http://localhost:8086/api/v1/reservas
    // http://localhost:8086/api/v1/reservas?idCliente=1
    // http://localhost:8086/api/v1/reservas?idUsuario=1
    // http://localhost:8086/api/v1/reservas?idHotel=1
    // http://localhost:8086/api/v1/reservas?idHabitacion=1
    // http://localhost:8086/api/v1/reservas?estadoReserva=PENDIENTE
    @GetMapping
    public ResponseEntity<?> findAll(
            @RequestParam(required = false) Long idCliente,
            @RequestParam(required = false) Long idUsuario,
            @RequestParam(required = false) Long idHotel,
            @RequestParam(required = false) Long idHabitacion,
            @RequestParam(required = false) String estadoReserva) {

        if (idCliente != null) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(reservaService.findByIdCliente(idCliente));
        }

        if (idUsuario != null) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(reservaService.findByIdUsuario(idUsuario));
        }

        if (idHotel != null) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(reservaService.findByIdHotel(idHotel));
        }

        if (idHabitacion != null) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(reservaService.findByIdHabitacion(idHabitacion));
        }

        if (estadoReserva != null) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(reservaService.findByEstadoReserva(estadoReserva));
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(reservaService.findAll());
    }

    // http://localhost:8086/api/v1/reservas/1
    @GetMapping("/{id}")
    public ResponseEntity<Reserva> findById(@PathVariable Long id) {

        Reserva reserva = reservaService.findById(id);

        return ResponseEntity.status(HttpStatus.OK).body(reserva);
    }

    // http://localhost:8086/api/v1/reservas
    @PostMapping
    public ResponseEntity<Reserva> guardarReserva(
            @Valid @RequestBody Reserva reserva
    ) {

        Reserva nuevaReserva = reservaService.guardar(reserva);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(nuevaReserva);
    }

    // http://localhost:8086/api/v1/reservas/1
    @PutMapping("/{id}")
    public ResponseEntity<Reserva> actualizarReserva(
            @PathVariable Long id,
            @Valid @RequestBody Reserva reserva
    ) {

        Reserva reservaActualizada =
                reservaService.actualizar(id, reserva);

        return ResponseEntity.status(HttpStatus.OK)
                .body(reservaActualizada);
    }

    // http://localhost:8086/api/v1/reservas/1
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarReserva(
            @PathVariable Long id
    ) {

        reservaService.eliminar(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .build();
    }
    /*
    //http://localhost:8086/api/v1/reservas/1/estado?estadoReserva=CONFIRMADA
    @PatchMapping("/{id}/estado")
    public ResponseEntity<Reserva> cambiarEstado(
            @PathVariable Long id,
            @RequestParam String estadoReserva
    ) {

        Reserva reservaActualizada =
                reservaService.cambiarEstado(id, estadoReserva);

        return ResponseEntity.ok(reservaActualizada);
    }
    */
    // http://localhost:8086/api/v1/reservas/1/estado?estadoReserva=CONFIRMADA
    @PutMapping("/{id}/estado")
    public ResponseEntity<Reserva> cambiarEstado(
            @PathVariable Long id,
            @RequestParam String estadoReserva
    ) {

        Reserva reservaActualizada =
                reservaService.cambiarEstado(id, estadoReserva);

        return ResponseEntity.ok(reservaActualizada);
    }
}