package com.example.ms_auth_usuarios_service.controller;

import com.example.ms_auth_usuarios_service.model.Usuario;
import com.example.ms_auth_usuarios_service.security.JwtService;
import com.example.ms_auth_usuarios_service.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioService usuarioService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Usuario usuario) {
        Usuario usuarioGuardado = usuarioService.save(usuario);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Usuario registrado correctamente",
                "idUsuario", usuarioGuardado.getIdUsuario(),
                "nombre", usuarioGuardado.getNombre(),
                "rol", usuarioGuardado.getRol()
        ));
    }

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
                    "rol", usuario.getRol()
            ));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "error", "Credenciales inválidas"
        ));
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken() {
        return ResponseEntity.ok(Map.of("valid", true));
    }
}