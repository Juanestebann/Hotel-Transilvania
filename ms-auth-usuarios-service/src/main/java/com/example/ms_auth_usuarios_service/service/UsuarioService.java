package com.example.ms_auth_usuarios_service.service;

import com.example.ms_auth_usuarios_service.model.Usuario;
import com.example.ms_auth_usuarios_service.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public List<Usuario> findAll() {
        log.info("Listando todos los usuarios");
        return usuarioRepository.findAll();
    }

    public Usuario findById(Long id) {
        log.info("Buscando usuario con id: {}", id);

        return usuarioRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Usuario no encontrado con id: {}", id);
                    return new NoSuchElementException("Usuario no encontrado con id: " + id);
                });
    }

    public List<Usuario> findByRol(String rol) {
        log.info("Buscando usuarios con rol: {}", rol);
        return usuarioRepository.findByRol(rol);
    }

    public Optional<Usuario> findByNombre(String nombre) {
        log.info("Buscando usuario con nombre: {}", nombre);
        return usuarioRepository.findByNombre(nombre);
    }

    public Usuario findAuthenticatedByNombre(String nombre) {
        log.info("Buscando identidad del usuario autenticado: {}", nombre);

        return usuarioRepository.findByNombre(nombre)
                .orElseThrow(() -> {
                    log.error("Usuario autenticado no encontrado con nombre: {}", nombre);
                    return new NoSuchElementException("Usuario autenticado no encontrado");
                });
    }

    public Usuario register(Usuario usuario) {
        log.info("Registro público de usuario: {}", usuario.getNombre());
        usuario.setRol("USER");
        return save(usuario);
    }

    public Usuario save(Usuario usuario) {
        log.info("Intentando crear usuario con nombre: {} y rol: {}", usuario.getNombre(), usuario.getRol());

        if (usuario.getIdUsuario() != null) {
            log.warn("Intento inválido de crear usuario enviando idUsuario: {}", usuario.getIdUsuario());
            throw new IllegalArgumentException("No debe enviar idUsuario al crear un usuario");
        }

        if (usuarioRepository.existsByNombre(usuario.getNombre())) {
            log.warn("Intento de crear usuario duplicado: {}", usuario.getNombre());
            throw new IllegalArgumentException("Ya existe un usuario con ese nombre");
        }

        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        if (usuario.getRol() == null || usuario.getRol().isBlank()) {
            usuario.setRol("USER");
        }

        validarRol(usuario.getRol());

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        log.info("Usuario creado correctamente con id: {}", usuarioGuardado.getIdUsuario());

        return usuarioGuardado;
    }

    public Usuario update(Long id, Usuario usuario) {
        log.info("Actualizando usuario con id: {}", id);

        Usuario usuarioExistente = findById(id);

        usuarioExistente.setNombre(usuario.getNombre());

        if (usuario.getPassword() != null && !usuario.getPassword().isBlank()) {
            usuarioExistente.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }

        validarRol(usuario.getRol());
        usuarioExistente.setRol(usuario.getRol());

        Usuario usuarioActualizado = usuarioRepository.save(usuarioExistente);

        log.info("Usuario actualizado correctamente con id: {}", usuarioActualizado.getIdUsuario());

        return usuarioActualizado;
    }

    public void delete(Long id) {
        log.warn("Solicitando eliminación de usuario con id: {}", id);

        Usuario usuario = findById(id);

        usuarioRepository.delete(usuario);

        log.warn("Usuario eliminado correctamente con id: {}", id);
    }

    private void validarRol(String rol) {
        if (!rol.equalsIgnoreCase("USER") && !rol.equalsIgnoreCase("ADMIN")) {
            throw new IllegalArgumentException("Rol inválido. Use: USER o ADMIN");
        }
    }
}
