package com.example.ms_reserva_service.controller;

import com.example.ms_reserva_service.model.Reserva;
import com.example.ms_reserva_service.service.ReservaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


/*ADMIN:
- listar reservas
- filtrar reservas
- actualizar reserva
- eliminar reserva
- cambiar estado

USER y ADMIN:
- ver reserva por id
- crear reserva

 */

@RestController
@RequestMapping("/api/v1/reservas")
@RequiredArgsConstructor
public class ReservaController {

    private final ReservaService reservaService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<?> findAll(
            @RequestParam(required = false) Long idCliente,
            @RequestParam(required = false) Long idUsuario,
            @RequestParam(required = false) Long idHotel,
            @RequestParam(required = false) Long idHabitacion,
            @RequestParam(required = false) String estadoReserva) {

        if (idCliente != null) {
            return ResponseEntity.status(HttpStatus.OK).body(reservaService.findByIdCliente(idCliente));
        }

        if (idUsuario != null) {
            return ResponseEntity.status(HttpStatus.OK).body(reservaService.findByIdUsuario(idUsuario));
        }

        if (idHotel != null) {
            return ResponseEntity.status(HttpStatus.OK).body(reservaService.findByIdHotel(idHotel));
        }

        if (idHabitacion != null) {
            return ResponseEntity.status(HttpStatus.OK).body(reservaService.findByIdHabitacion(idHabitacion));
        }

        if (estadoReserva != null) {
            return ResponseEntity.status(HttpStatus.OK).body(reservaService.findByEstadoReserva(estadoReserva));
        }

        return ResponseEntity.status(HttpStatus.OK).body(reservaService.findAll());
    }


    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<Reserva> findById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(reservaService.findById(id));
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<Reserva> guardarReserva(@Valid @RequestBody Reserva reserva) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservaService.guardar(reserva));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Reserva> actualizarReserva(
            @PathVariable Long id,
            @Valid @RequestBody Reserva reserva) {

        return ResponseEntity.status(HttpStatus.OK).body(reservaService.actualizar(id, reserva));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarReserva(@PathVariable Long id) {
        reservaService.eliminar(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PutMapping("/{id}/estado")
    public ResponseEntity<Reserva> cambiarEstado(
            @PathVariable Long id,
            @RequestParam String estadoReserva) {

        return ResponseEntity.ok(reservaService.cambiarEstado(id, estadoReserva));
    }
}