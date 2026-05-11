package com.example.ms_pago_service.dto;
import lombok.Data;

@Data
public class UsuarioDTO {

    private Long idUsuario;
    private String nombre;
    private String password;
    private String rol;
}