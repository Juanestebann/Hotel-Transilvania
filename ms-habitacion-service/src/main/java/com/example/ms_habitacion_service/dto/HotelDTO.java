package com.example.ms_habitacion_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotelDTO {

    private Long id;

    private String nombre;

    private String direccion;

    private String ciudad;

    private String pais;

    private String categoria;

    private String descripcion;
}
