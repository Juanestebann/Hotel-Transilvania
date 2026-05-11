package com.example.ms_pago_service.dto;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ReservaDTO {

    private Long id;
    private Long idCliente;
    private Long idUsuario;
    private Long idHotel;
    private Long idHabitacion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Integer cantidadPersonas;
    private String estadoReserva;
    private LocalDateTime fechaCreacion;
}