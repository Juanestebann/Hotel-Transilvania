package com.example.ms_pago_service.controller;

import com.example.ms_pago_service.model.Pago;
import com.example.ms_pago_service.service.PagoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/v1/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;

    // http://localhost:8087/api/v1/pagos
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<Pago>> findAll() {
        List<Pago> pagos = pagoService.findAll();

        pagos.forEach(this::agregarLinksPago);

        return ResponseEntity.ok(pagos);
    }

    // http://localhost:8087/api/v1/pagos/1
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<Pago> findById(@PathVariable Long id) {
        Pago pago = pagoService.findById(id);

        agregarLinksPago(pago);

        return ResponseEntity.ok(pago);
    }

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
    public ResponseEntity<List<Pago>> findByReservaId(@PathVariable Long reservaId) {

        List<Pago> pagos = pagoService.findByReservaId(reservaId);

        pagos.forEach(this::agregarLinksPago);

        return ResponseEntity.ok(pagos);
    }

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