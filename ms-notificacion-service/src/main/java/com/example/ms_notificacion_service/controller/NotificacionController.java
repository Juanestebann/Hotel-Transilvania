package com.example.ms_notificacion_service.controller;

import com.example.ms_notificacion_service.model.Notificacion;
import com.example.ms_notificacion_service.service.NotificacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;

    @GetMapping
    public ResponseEntity<?> findAll(
            @RequestParam(required = false) Long idCliente,
            @RequestParam(required = false) Long idUsuario,
            @RequestParam(required = false) Long idReserva
    ) {
        //http://localhost:8088/api/v1/notificaciones?idCliente=1
        if (idCliente != null) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(notificacionService.findByIdCliente(idCliente));
        }

        //http://localhost:8088/api/v1/notificaciones?idUsuario=1
        if (idUsuario != null) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(notificacionService.findByIdUsuario(idUsuario));
        }

        //http://localhost:8088/api/v1/notificaciones?idReserva=1
        if (idReserva != null) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(notificacionService.findByIdReserva(idReserva));
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(notificacionService.findAll());
    }

    //http://localhost:8088/api/v1/notificaciones/1
    @GetMapping("/{id}")
    public ResponseEntity<Notificacion> findById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(notificacionService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Notificacion> guardar(@Valid @RequestBody Notificacion notificacion) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(notificacionService.guardar(notificacion));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Notificacion> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody Notificacion notificacion
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(notificacionService.actualizar(id, notificacion));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        notificacionService.eliminar(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}