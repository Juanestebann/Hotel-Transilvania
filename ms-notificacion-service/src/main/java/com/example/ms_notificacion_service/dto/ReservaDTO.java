package com.example.ms_notificacion_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservaDTO {

    private Long id;
    private Long idCliente;
    private Long idUsuario;
    private Long idHotel;
    private Long idHabitacion;
    private String estadoReserva;
}