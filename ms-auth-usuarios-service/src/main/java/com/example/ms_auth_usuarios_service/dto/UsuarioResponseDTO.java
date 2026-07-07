package com.example.ms_auth_usuarios_service.dto;

import com.example.ms_auth_usuarios_service.model.Usuario;

public record UsuarioResponseDTO(
        Long idUsuario,
        String nombre,
        String rol
) {

    public static UsuarioResponseDTO from(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getIdUsuario(),
                usuario.getNombre(),
                usuario.getRol()
        );
    }
}
