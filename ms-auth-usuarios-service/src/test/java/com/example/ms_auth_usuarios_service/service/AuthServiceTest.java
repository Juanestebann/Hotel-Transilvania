package com.example.ms_auth_usuarios_service.service;

import com.example.ms_auth_usuarios_service.model.Usuario;
import com.example.ms_auth_usuarios_service.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void deberiaListarUsuarios() {
        Mockito.when(usuarioRepository.findAll()).thenReturn(List.of(crearUsuario()));

        List<Usuario> resultado = usuarioService.findAll();

        assertEquals(1, resultado.size());
        verify(usuarioRepository).findAll();
    }

    @Test
    void deberiaRetornarUsuarioCuandoExiste() {
        Mockito.when(usuarioRepository.findById(1L)).thenReturn(Optional.of(crearUsuario()));

        Usuario resultado = usuarioService.findById(1L);

        assertEquals(1L, resultado.getIdUsuario());
        assertEquals("JuanAdmin", resultado.getNombre());
        verify(usuarioRepository).findById(1L);
    }

    @Test
    void deberiaLanzarErrorCuandoUsuarioNoExiste() {
        Mockito.when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> usuarioService.findById(99L));

        verify(usuarioRepository).findById(99L);
    }

    @Test
    void deberiaBuscarPorRol() {
        Mockito.when(usuarioRepository.findByRol("ADMIN")).thenReturn(List.of(crearUsuario()));

        List<Usuario> resultado = usuarioService.findByRol("ADMIN");

        assertEquals(1, resultado.size());
        assertEquals("ADMIN", resultado.get(0).getRol());
        verify(usuarioRepository).findByRol("ADMIN");
    }

    @Test
    void deberiaBuscarPorNombre() {
        Mockito.when(usuarioRepository.findByNombre("JuanAdmin")).thenReturn(Optional.of(crearUsuario()));

        Optional<Usuario> resultado = usuarioService.findByNombre("JuanAdmin");

        assertTrue(resultado.isPresent());
        assertEquals("JuanAdmin", resultado.get().getNombre());
        verify(usuarioRepository).findByNombre("JuanAdmin");
    }

    @Test
    void deberiaRetornarUsuarioAutenticadoPorNombre() {
        Mockito.when(usuarioRepository.findByNombre("JuanAdmin")).thenReturn(Optional.of(crearUsuario()));

        Usuario resultado = usuarioService.findAuthenticatedByNombre("JuanAdmin");

        assertEquals(1L, resultado.getIdUsuario());
        assertEquals("JuanAdmin", resultado.getNombre());
        verify(usuarioRepository).findByNombre("JuanAdmin");
    }

    @Test
    void deberiaLanzarErrorCuandoUsuarioAutenticadoNoExiste() {
        Mockito.when(usuarioRepository.findByNombre("desconocido")).thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> usuarioService.findAuthenticatedByNombre("desconocido")
        );

        verify(usuarioRepository).findByNombre("desconocido");
    }

    @Test
    void deberiaCrearUsuario() {
        Usuario usuario = crearUsuarioSinId();

        Mockito.when(usuarioRepository.existsByNombre("JuanAdmin")).thenReturn(false);
        Mockito.when(passwordEncoder.encode("123456")).thenReturn("password-encriptada");

        Usuario guardado = crearUsuario();
        guardado.setPassword("password-encriptada");

        Mockito.when(usuarioRepository.save(usuario)).thenReturn(guardado);

        Usuario resultado = usuarioService.save(usuario);

        assertEquals(1L, resultado.getIdUsuario());
        assertEquals("password-encriptada", resultado.getPassword());
        verify(usuarioRepository).existsByNombre("JuanAdmin");
        verify(passwordEncoder).encode("123456");
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void deberiaRegistrarUsuarioConRolUser() {
        Usuario usuario = crearUsuarioSinId();
        usuario.setRol(null);

        Mockito.when(usuarioRepository.existsByNombre("JuanAdmin")).thenReturn(false);
        Mockito.when(passwordEncoder.encode("123456")).thenReturn("password-encriptada");

        Usuario guardado = crearUsuario();
        guardado.setRol("USER");
        guardado.setPassword("password-encriptada");

        Mockito.when(usuarioRepository.save(usuario)).thenReturn(guardado);

        Usuario resultado = usuarioService.register(usuario);

        assertEquals("USER", resultado.getRol());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void deberiaLanzarErrorCuandoNombreYaExiste() {
        Usuario usuario = crearUsuarioSinId();

        Mockito.when(usuarioRepository.existsByNombre("JuanAdmin")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> usuarioService.save(usuario));

        verify(usuarioRepository).existsByNombre("JuanAdmin");
    }

    @Test
    void deberiaLanzarErrorCuandoSeEnviaIdAlCrear() {
        Usuario usuario = crearUsuario();

        assertThrows(IllegalArgumentException.class, () -> usuarioService.save(usuario));
    }

    @Test
    void deberiaActualizarUsuario() {
        Usuario existente = crearUsuario();
        Usuario actualizado = crearUsuarioSinId();
        actualizado.setNombre("JuanEditado");
        actualizado.setPassword("nuevaClave");
        actualizado.setRol("USER");

        Mockito.when(usuarioRepository.findById(1L)).thenReturn(Optional.of(existente));
        Mockito.when(passwordEncoder.encode("nuevaClave")).thenReturn("clave-encriptada");
        Mockito.when(usuarioRepository.save(existente)).thenReturn(existente);

        Usuario resultado = usuarioService.update(1L, actualizado);

        assertEquals("JuanEditado", resultado.getNombre());
        assertEquals("clave-encriptada", resultado.getPassword());
        assertEquals("USER", resultado.getRol());

        verify(usuarioRepository).findById(1L);
        verify(passwordEncoder).encode("nuevaClave");
        verify(usuarioRepository).save(existente);
    }

    @Test
    void deberiaEliminarUsuario() {
        Usuario usuario = crearUsuario();

        Mockito.when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        usuarioService.delete(1L);

        verify(usuarioRepository).findById(1L);
        verify(usuarioRepository).delete(usuario);
    }

    private Usuario crearUsuario() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1L);
        usuario.setNombre("JuanAdmin");
        usuario.setPassword("123456");
        usuario.setRol("ADMIN");
        return usuario;
    }

    private Usuario crearUsuarioSinId() {
        Usuario usuario = new Usuario();
        usuario.setNombre("JuanAdmin");
        usuario.setPassword("123456");
        usuario.setRol("ADMIN");
        return usuario;
    }
}
