package com.example.ms_hotel_service.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "hotel")

public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idHotel")
    private Long id;
    @Column(nullable = false)
    @NotNull
    private String nombre;
    @Column(nullable = false)
    @NotBlank
    private String direccion;
    @NotBlank
    private String ciudad;
    @NotBlank
    private String pais;
    @Column(nullable = false)
    @NotBlank
    private String categoria;
    @NotBlank
    private String descripcion;
}
