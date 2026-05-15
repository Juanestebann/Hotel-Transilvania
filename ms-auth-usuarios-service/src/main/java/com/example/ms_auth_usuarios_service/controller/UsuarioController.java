package com.example.ms_auth_usuarios_service.controller;
import com.example.ms_auth_usuarios_service.model.Usuario;
import com.example.ms_auth_usuarios_service.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    //http://localhost:8081/api/v1/usuarios
    @GetMapping
    public ResponseEntity<?> findAll() {
        return ResponseEntity.ok(usuarioService.findAll());
    }
    //http://localhost:8081/api/v1/usuarios/1
    //http://localhost:8081/api/v1/usuarios/9999 --> Usuario Inexistente
    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.findById(id));
    }
    //http://localhost:8081/api/v1/usuarios/rol/ADMIN
    @GetMapping("/rol/{rol}")
    public ResponseEntity<?> findByRol(@PathVariable String rol) {
        return ResponseEntity.ok(usuarioService.findByRol(rol));
    }
    //http://localhost:8081/api/v1/usuarios --> Probar crear y validaciones.
    @PostMapping
    public ResponseEntity<?> save(@Valid @RequestBody Usuario usuario) {
        Usuario usuarioGuardado = usuarioService.save(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioGuardado);
    }
    //http://localhost:8081/api/v1/usuarios/1
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody Usuario usuario) {
        return ResponseEntity.ok(usuarioService.update(id, usuario));
    }
    //http://localhost:8081/api/v1/usuarios/5
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        usuarioService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
