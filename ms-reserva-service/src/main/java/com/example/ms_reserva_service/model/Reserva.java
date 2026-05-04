package com.example.ms_reserva_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "reserva")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idReserva")
    private Long id;

    @Column(nullable = false)
    @NotNull(message = "El idCliente es obligatorio")
    private Long idCliente;

    @Column(nullable = false)
    @NotNull(message = "El idHotel es obligatorio")
    private Long idHotel;

    @Column(nullable = false)
    @NotNull(message = "El idHabitacion es obligatorio")
    private Long idHabitacion;

    @Column(nullable = false)
    @NotNull(message = "La fechaInicio es obligatoria")
    private LocalDate fechaInicio;

    @Column(nullable = false)
    @NotNull(message = "La fechaFin es obligatoria")
    private LocalDate fechaFin;

    @Column(nullable = false)
    @NotNull(message = "La cantidadPersonas es obligatoria")
    @Min(value = 1, message = "La cantidadPersonas debe ser al menos 1")
    private Integer cantidadPersonas;

    @Column(nullable = false)
    @NotBlank(message = "El estadoReserva es obligatorio")
    private String estadoReserva;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;
}