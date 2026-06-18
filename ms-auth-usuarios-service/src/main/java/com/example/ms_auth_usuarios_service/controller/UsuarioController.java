package com.example.ms_auth_usuarios_service.controller;

import com.example.ms_auth_usuarios_service.model.Usuario;
import com.example.ms_auth_usuarios_service.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    // Solo ADMIN puede listar todos los usuarios
    // http://localhost:8081/api/v1/usuarios
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<Usuario>> findAll() {
        List<Usuario> usuarios = usuarioService.findAll();

        usuarios.forEach(this::agregarLinksUsuario);

        return ResponseEntity.ok(usuarios);
    }

    // Solo ADMIN puede buscar usuarios por ID
    // http://localhost:8081/api/v1/usuarios/1
    // http://localhost:8081/api/v1/usuarios/9999 --> Usuario inexistente
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> findById(@PathVariable Long id) {
        Usuario usuario = usuarioService.findById(id);

        agregarLinksUsuario(usuario);

        return ResponseEntity.ok(usuario);
    }

    // Solo ADMIN puede buscar usuarios por rol
    // http://localhost:8081/api/v1/usuarios/rol/ADMIN
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/rol/{rol}")
    public ResponseEntity<List<Usuario>> findByRol(@PathVariable String rol) {
        List<Usuario> usuarios = usuarioService.findByRol(rol);

        usuarios.forEach(this::agregarLinksUsuario);

        return ResponseEntity.ok(usuarios);
    }

    // Solo ADMIN puede crear usuarios manualmente
    // http://localhost:8081/api/v1/usuarios
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Usuario> save(@Valid @RequestBody Usuario usuario) {
        Usuario usuarioGuardado = usuarioService.save(usuario);

        agregarLinksUsuario(usuarioGuardado);

        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioGuardado);
    }

    // Solo ADMIN puede actualizar usuarios
    // http://localhost:8081/api/v1/usuarios/1
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Usuario> update(@PathVariable Long id, @Valid @RequestBody Usuario usuario) {
        Usuario usuarioActualizado = usuarioService.update(id, usuario);

        agregarLinksUsuario(usuarioActualizado);

        return ResponseEntity.ok(usuarioActualizado);
    }

    // Solo ADMIN puede eliminar usuarios
    // http://localhost:8081/api/v1/usuarios/5
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        usuarioService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Agrega enlaces HATEOAS a un usuario.
     * Permite navegar entre recursos relacionados de forma dinámica.
     */
    private void agregarLinksUsuario(Usuario usuario) {

        // Link al recurso actual (el propio usuario)
        usuario.add(linkTo(methodOn(UsuarioController.class)
                .findById(usuario.getIdUsuario())).withSelfRel());

        // Link para listar todos los usuarios
        usuario.add(linkTo(methodOn(UsuarioController.class)
                .findAll()).withRel("todos-los-usuarios"));

        // Link para listar usuarios con el mismo rol
        usuario.add(linkTo(methodOn(UsuarioController.class)
                .findByRol(usuario.getRol())).withRel("usuarios-por-rol"));

        // Link para actualizar este usuario
        usuario.add(linkTo(methodOn(UsuarioController.class)
                .update(usuario.getIdUsuario(), null)).withRel("actualizar-usuario"));

        // Link para eliminar este usuario
        usuario.add(linkTo(methodOn(UsuarioController.class)
                .delete(usuario.getIdUsuario())).withRel("eliminar-usuario"));
    }
}