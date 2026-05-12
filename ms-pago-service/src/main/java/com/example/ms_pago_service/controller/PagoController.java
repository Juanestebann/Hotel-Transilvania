package com.example.ms_pago_service.controller;

import com.example.ms_pago_service.model.Pago;
import com.example.ms_pago_service.service.PagoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;

    @GetMapping
    public ResponseEntity<?> findAll() {
        return ResponseEntity.ok(pagoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        return ResponseEntity.ok(pagoService.findById(id));
    }

    @PostMapping
    public ResponseEntity<?> save(@Valid @RequestBody Pago pago) {

        Pago nuevoPago = pagoService.save(pago);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(nuevoPago);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @Valid @RequestBody Pago pago) {

        return ResponseEntity.ok(
                pagoService.update(id, pago)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {

        pagoService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reserva/{reservaId}")
    public ResponseEntity<?> findByReservaId(@PathVariable Long reservaId) {

        return ResponseEntity.ok(
                pagoService.findByReservaId(reservaId)
        );
    }

    @GetMapping("/estado/{estadoPago}")
    public ResponseEntity<?> findByEstadoPago(@PathVariable String estadoPago) {

        return ResponseEntity.ok(
                pagoService.findByEstadoPago(estadoPago)
        );
    }
}