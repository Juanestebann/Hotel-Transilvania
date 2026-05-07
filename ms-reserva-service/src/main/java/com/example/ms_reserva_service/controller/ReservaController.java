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

    @GetMapping("/{id}")
    public ResponseEntity<Reserva> findById(@PathVariable Long id) {
        Reserva reserva = reservaService.findById(id);
        return ResponseEntity.status(HttpStatus.OK).body(reserva);
    }

    @PostMapping
    public ResponseEntity<Reserva> guardarReserva(@Valid @RequestBody Reserva reserva) {
        Reserva nuevaReserva = reservaService.guardar(reserva);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaReserva);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Reserva> actualizarReserva(@PathVariable Long id,
                                                     @Valid @RequestBody Reserva reserva) {
        Reserva reservaActualizada = reservaService.actualizar(id, reserva);
        return ResponseEntity.status(HttpStatus.OK).body(reservaActualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarReserva(@PathVariable Long id) {
        reservaService.eliminar(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}