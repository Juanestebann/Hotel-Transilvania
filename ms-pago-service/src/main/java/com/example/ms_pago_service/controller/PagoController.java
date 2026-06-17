package com.example.ms_pago_service.controller;

import com.example.ms_pago_service.model.Pago;
import com.example.ms_pago_service.service.PagoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;

    // http://localhost:8087/api/v1/pagos
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<?> findAll() {
        return ResponseEntity.ok(pagoService.findAll());
    }

    // http://localhost:8087/api/v1/pagos/1
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        return ResponseEntity.ok(pagoService.findById(id));
    }

    // http://localhost:8087/api/v1/pagos
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<?> save(@Valid @RequestBody Pago pago) {

        Pago nuevoPago = pagoService.save(pago);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(nuevoPago);
    }

    // http://localhost:8087/api/v1/pagos/1
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @Valid @RequestBody Pago pago) {

        return ResponseEntity.ok(
                pagoService.update(id, pago)
        );
    }

    // http://localhost:8087/api/v1/pagos/1
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {

        pagoService.delete(id);

        return ResponseEntity.noContent().build();
    }

    // http://localhost:8087/api/v1/pagos/reserva/1
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/reserva/{reservaId}")
    public ResponseEntity<?> findByReservaId(@PathVariable Long reservaId) {

        return ResponseEntity.ok(
                pagoService.findByReservaId(reservaId)
        );
    }

    // http://localhost:8087/api/v1/pagos/estado/APROBADO
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/estado/{estadoPago}")
    public ResponseEntity<?> findByEstadoPago(@PathVariable String estadoPago) {

        return ResponseEntity.ok(
                pagoService.findByEstadoPago(estadoPago)
        );
    }
}