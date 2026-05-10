package com.example.ms_reserva_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HabitacionDTO {

    private Long idHabitacion;
    private String numeroHabitacion;
    private String tipoHabitacion;
    private Double precioBase;
    private Integer capacidad;
    private String estadoHabitacion;
    private Long idHotel;
}
