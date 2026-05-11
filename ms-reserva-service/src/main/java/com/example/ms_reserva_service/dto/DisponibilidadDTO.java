package com.example.ms_reserva_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DisponibilidadDTO {

    private Long id;
    private Long idHabitacion;
    private LocalDate fecha;
    private String estado;
}