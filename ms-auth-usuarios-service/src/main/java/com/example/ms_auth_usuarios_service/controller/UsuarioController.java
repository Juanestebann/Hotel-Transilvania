package com.example.ms_auth_usuarios_service.controller;

import com.example.ms_auth_usuarios_service.model.Usuario;
import com.example.ms_auth_usuarios_service.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    // Solo ADMIN puede listar todos los usuarios
    // http://localhost:8081/api/v1/usuarios
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<?> findAll() {
        return ResponseEntity.ok(usuarioService.findAll());
    }

    // Solo ADMIN puede buscar usuarios por ID
    // http://localhost:8081/api/v1/usuarios/1
    // http://localhost:8081/api/v1/usuarios/9999 --> Usuario inexistente
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.findById(id));
    }

    // Solo ADMIN puede buscar usuarios por rol
    // http://localhost:8081/api/v1/usuarios/rol/ADMIN
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/rol/{rol}")
    public ResponseEntity<?> findByRol(@PathVariable String rol) {
        return ResponseEntity.ok(usuarioService.findByRol(rol));
    }

    // Solo ADMIN puede crear usuarios manualmente
    // http://localhost:8081/api/v1/usuarios
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> save(@Valid @RequestBody Usuario usuario) {
        Usuario usuarioGuardado = usuarioService.save(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioGuardado);
    }

    // Solo ADMIN puede actualizar usuarios
    // http://localhost:8081/api/v1/usuarios/1
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody Usuario usuario) {
        return ResponseEntity.ok(usuarioService.update(id, usuario));
    }

    // Solo ADMIN puede eliminar usuarios
    // http://localhost:8081/api/v1/usuarios/5
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        usuarioService.delete(id);
        return ResponseEntity.noContent().build();
    }
}