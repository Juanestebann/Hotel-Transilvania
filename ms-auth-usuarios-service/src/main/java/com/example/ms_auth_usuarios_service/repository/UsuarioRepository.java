package com.example.ms_auth_usuarios_service.repository;

import com.example.ms_auth_usuarios_service.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    List<Usuario> findByRol(String rol);

    boolean existsByNombre(String nombre);
}