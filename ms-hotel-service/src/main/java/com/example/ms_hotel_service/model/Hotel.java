package com.example.ms_hotel_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "hotel")
public class Hotel extends RepresentationModel<Hotel> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idHotel")
    private Long id;

    @NotBlank(message = "El nombre del hotel es obligatorio")
    @Column(nullable = false, length = 50)
    private String nombre;

    @NotBlank(message = "La dirección del hotel es obligatoria")
    @Column(nullable = false, length = 100)
    private String direccion;

    @NotBlank(message = "La ciudad del hotel es obligatoria")
    @Column(nullable = false, length = 50)
    private String ciudad;

    @NotBlank(message = "El país del hotel es obligatorio")
    @Column(nullable = false, length = 50)
    private String pais;

    @NotBlank(message = "La categoría del hotel es obligatoria")
    @Column(nullable = false, length = 30)
    private String categoria;

    @NotBlank(message = "La descripción del hotel es obligatoria")
    @Column(nullable = false, length = 255)
    private String descripcion;
}
