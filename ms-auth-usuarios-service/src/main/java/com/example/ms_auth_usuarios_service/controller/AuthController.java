package com.example.ms_auth_usuarios_service.controller;

import com.example.ms_auth_usuarios_service.dto.RegisterRequest;
import com.example.ms_auth_usuarios_service.model.Usuario;
import com.example.ms_auth_usuarios_service.security.JwtService;
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
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Tag(
        name = "Autenticación",
        description = "Endpoints públicos para registro e inicio de sesión, más validación de JWT Bearer"
)
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioService usuarioService;

    @Operation(
            summary = "Registrar usuario",
            description = "Crea un usuario público con nombre y contraseña. El servicio asigna el rol permitido por la lógica de registro.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = RegisterRequest.class),
                            examples = @ExampleObject(
                                    name = "Registro",
                                    value = """
                                            {
                                              "nombre": "juan",
                                              "password": "clave123"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario registrado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos, nombre duplicado o rol inválido", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error interno no controlado", content = @Content)
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setPassword(request.getPassword());
        Usuario usuarioGuardado = usuarioService.register(usuario);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Usuario registrado correctamente",
                "idUsuario", usuarioGuardado.getIdUsuario(),
                "nombre", usuarioGuardado.getNombre(),
                "rol", usuarioGuardado.getRol(),
                "_links", Map.of(
                        "login", linkTo(methodOn(AuthController.class).login(null)).withRel("login").getHref(),
                        "validate", linkTo(methodOn(AuthController.class).validateToken()).withRel("validate-token").getHref(),
                        "usuario", linkTo(methodOn(UsuarioController.class).findById(usuarioGuardado.getIdUsuario())).withRel("usuario-creado").getHref()
                )
        ));
    }

    @Operation(
            summary = "Iniciar sesión",
            description = "Autentica credenciales públicas y devuelve un token JWT Bearer con nombre y rol del usuario.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    name = "Login",
                                    value = """
                                            {
                                              "nombre": "juan",
                                              "password": "clave123"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticación correcta y token emitido"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida o credenciales incompletas", content = @Content),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {

        String nombre = body.get("nombre");
        String password = body.get("password");

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(nombre, password)
        );

        if (authentication.isAuthenticated()) {
            Usuario usuario = usuarioService.findByNombre(nombre)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            String token = jwtService.generateToken(usuario.getNombre(), usuario.getRol());

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "nombre", usuario.getNombre(),
                    "rol", usuario.getRol(),
                    "_links", Map.of(
                            "validate", linkTo(methodOn(AuthController.class).validateToken()).withRel("validate-token").getHref(),
                            "usuario", linkTo(methodOn(UsuarioController.class).findById(usuario.getIdUsuario())).withRel("usuario").getHref()
                    )
            ));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "error", "Credenciales inválidas",
                "_links", Map.of(
                        "login", linkTo(methodOn(AuthController.class).login(null)).withRel("login").getHref()
                )
        ));
    }

    @Operation(
            summary = "Validar token JWT",
            description = "Confirma que el token Bearer enviado sea válido. Requiere JWT de usuario autenticado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token válido"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken() {
        return ResponseEntity.ok(Map.of(
                "valid", true,
                "_links", Map.of(
                        "login", linkTo(methodOn(AuthController.class).login(null)).withRel("login").getHref(),
                        "register", linkTo(methodOn(AuthController.class).register(null)).withRel("register").getHref()
                )
        ));
    }
}
