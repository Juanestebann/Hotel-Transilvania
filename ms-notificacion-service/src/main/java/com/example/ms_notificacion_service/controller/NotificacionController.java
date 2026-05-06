package com.example.ms_notificacion_service.controller;
import com.example.ms_notificacion_service.model.Notificacion;
import com.example.ms_notificacion_service.service.NotificacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;

    @GetMapping
    public ResponseEntity<List<Notificacion>> findAll() {
        return ResponseEntity.ok(notificacionService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Notificacion> findById(@PathVariable Long id) {
        return ResponseEntity.ok(notificacionService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Notificacion> guardar(@Valid @RequestBody Notificacion notificacion) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificacionService.guardar(notificacion));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Notificacion> actualizar(@PathVariable Long id, @Valid @RequestBody Notificacion notificacion) {
        return ResponseEntity.ok(notificacionService.actualizar(id, notificacion));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        notificacionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}