package com.example.ms_auth_usuarios_service.controller;

import com.example.ms_auth_usuarios_service.dto.UsuarioResponseDTO;
import com.example.ms_auth_usuarios_service.model.Usuario;
import com.example.ms_auth_usuarios_service.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Tag(
        name = "Usuarios",
        description = "Operaciones protegidas para consultar y administrar usuarios del sistema"
)
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @Operation(
            summary = "Obtener usuario autenticado",
            description = "Devuelve los datos básicos del usuario asociado al token JWT. Requiere rol USER o ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario autenticado encontrado"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuario sin permisos para consultar este recurso", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario autenticado no encontrado", content = @Content)
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> findCurrentUser(Authentication authentication) {
        Usuario usuario = usuarioService.findAuthenticatedByNombre(authentication.getName());
        return ResponseEntity.ok(UsuarioResponseDTO.from(usuario));
    }

    @Operation(
            summary = "Listar usuarios",
            description = "Lista todos los usuarios registrados. Requiere rol ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuarios listados correctamente"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuario sin permisos de ADMIN", content = @Content)
    })
    // Solo ADMIN puede listar todos los usuarios
    // http://localhost:8081/api/v1/usuarios
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<Usuario>> findAll() {
        List<Usuario> usuarios = usuarioService.findAll();

        usuarios.forEach(this::agregarLinksUsuario);

        return ResponseEntity.ok(usuarios);
    }

    @Operation(
            summary = "Buscar usuario por ID",
            description = "Obtiene un usuario por su identificador. Requiere rol ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuario sin permisos de ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
    })
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

    @Operation(
            summary = "Buscar usuarios por rol",
            description = "Lista usuarios filtrados por rol, por ejemplo ADMIN o USER. Requiere rol ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuarios filtrados correctamente"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuario sin permisos de ADMIN", content = @Content)
    })
    // Solo ADMIN puede buscar usuarios por rol
    // http://localhost:8081/api/v1/usuarios/rol/ADMIN
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/rol/{rol}")
    public ResponseEntity<List<Usuario>> findByRol(@PathVariable String rol) {
        List<Usuario> usuarios = usuarioService.findByRol(rol);

        usuarios.forEach(this::agregarLinksUsuario);

        return ResponseEntity.ok(usuarios);
    }

    @Operation(
            summary = "Crear usuario",
            description = "Crea un usuario manualmente con rol USER o ADMIN. Requiere rol ADMIN.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = Usuario.class),
                            examples = @ExampleObject(
                                    name = "Usuario",
                                    value = """
                                            {
                                              "nombre": "admin2",
                                              "password": "clave123",
                                              "rol": "ADMIN"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos, id enviado, nombre duplicado o rol inválido", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuario sin permisos de ADMIN", content = @Content)
    })
    // Solo ADMIN puede crear usuarios manualmente
    // http://localhost:8081/api/v1/usuarios
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Usuario> save(@Valid @RequestBody Usuario usuario) {
        Usuario usuarioGuardado = usuarioService.save(usuario);

        agregarLinksUsuario(usuarioGuardado);

        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioGuardado);
    }

    @Operation(
            summary = "Actualizar usuario",
            description = "Actualiza nombre, contraseña o rol de un usuario existente. Requiere rol ADMIN.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = Usuario.class),
                            examples = @ExampleObject(
                                    name = "Usuario actualizado",
                                    value = """
                                            {
                                              "nombre": "usuario1",
                                              "password": "nuevaClave123",
                                              "rol": "USER"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o rol inválido", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuario sin permisos de ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
    })
    // Solo ADMIN puede actualizar usuarios
    // http://localhost:8081/api/v1/usuarios/1
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Usuario> update(@PathVariable Long id, @Valid @RequestBody Usuario usuario) {
        Usuario usuarioActualizado = usuarioService.update(id, usuario);

        agregarLinksUsuario(usuarioActualizado);

        return ResponseEntity.ok(usuarioActualizado);
    }

    @Operation(
            summary = "Eliminar usuario",
            description = "Elimina un usuario por su identificador. Requiere rol ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuario eliminado correctamente", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuario sin permisos de ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
    })
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
