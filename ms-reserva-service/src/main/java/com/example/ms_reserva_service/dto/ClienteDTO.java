package com.example.ms_reserva_service.dto;

import lombok.Data;

@Data
public class ClienteDTO {

    private Long id;
    private String rutDocumento;
    private String telefono;
    private String direccion;
    private String rolCliente;
    private String tipoCliente;
}