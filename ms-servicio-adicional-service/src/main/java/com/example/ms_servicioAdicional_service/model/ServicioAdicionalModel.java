package com.example.ms_servicioAdicional_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "servicio_adicional")
public class ServicioAdicionalModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idServicio")
    private Long id;

    @Column(nullable = false)
    @NotNull(message = "El campo idHotel es obligatorio")
    private Long idHotel;

    @Column(nullable = true)
    private Long idReserva;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "El campo nombre es obligatorio")
    private String nombre;

    @Column(nullable = false, length = 200)
    @NotBlank(message = "El campo descripcion es obligatorio")
    private String descripcion;

    @Column(nullable = false)
    @NotNull(message = "El campo precio es obligatorio")
    private BigDecimal precio;

    @Column(nullable = false, length = 20)
    @NotBlank(message = "El campo estado es obligatorio")
    private String estado;

}