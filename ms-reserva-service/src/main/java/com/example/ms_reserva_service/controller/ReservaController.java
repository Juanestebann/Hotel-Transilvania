package com.example.ms_reserva_service.controller;

import com.example.ms_reserva_service.model.Reserva;
import com.example.ms_reserva_service.service.ReservaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/v1/reservas")
@RequiredArgsConstructor
public class ReservaController {

    private final ReservaService reservaService;

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

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<Reserva> findById(@PathVariable Long id) {
        Reserva reserva = reservaService.findById(id);
        agregarLinksReserva(reserva);

        return ResponseEntity.status(HttpStatus.OK).body(reserva);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<Reserva> guardarReserva(@Valid @RequestBody Reserva reserva) {
        Reserva reservaGuardada = reservaService.guardar(reserva);
        agregarLinksReserva(reservaGuardada);

        return ResponseEntity.status(HttpStatus.CREATED).body(reservaGuardada);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Reserva> actualizarReserva(
            @PathVariable Long id,
            @Valid @RequestBody Reserva reserva) {

        Reserva reservaActualizada = reservaService.actualizar(id, reserva);
        agregarLinksReserva(reservaActualizada);

        return ResponseEntity.status(HttpStatus.OK).body(reservaActualizada);
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

        Reserva reservaActualizada = reservaService.cambiarEstado(id, estadoReserva);
        agregarLinksReserva(reservaActualizada);

        return ResponseEntity.status(HttpStatus.OK).body(reservaActualizada);
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