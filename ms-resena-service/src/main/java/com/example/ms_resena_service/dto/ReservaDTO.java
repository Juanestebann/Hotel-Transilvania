package com.example.ms_resena_service.dto;

import lombok.Data;

@Data
public class ReservaDTO {
    private Long id;
    private Long idUsuario;
    private Long idCliente;
    private Long idHotel;
    private Long idHabitacion;
    private String estadoReserva;
}
