package com.example.ms_habitacion_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "habitacion")
public class Habitacion extends RepresentationModel<Habitacion> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idHabitacion")
    private Long idHabitacion;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "El número de habitación es obligatorio")
    private String numeroHabitacion;

    @Column(nullable = false)
    @NotBlank(message = "El tipo de habitación es obligatorio")
    private String tipoHabitacion;

    @Column(nullable = false)
    @NotNull(message = "El precio base es obligatorio")
    private Double precioBase;

    @Column(nullable = false)
    @NotNull(message = "La capacidad es obligatoria")
    private Integer capacidad;

    @Column(nullable = false)
    @NotBlank(message = "El estado de la habitación es obligatorio")
    private String estadoHabitacion;

    @Column(nullable = false)
    @NotNull(message = "El id del hotel es obligatorio")
    private Long idHotel;
}