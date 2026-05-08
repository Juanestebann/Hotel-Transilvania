package com.example.ms_reserva_service.dto;
import lombok.Data;

@Data
public class HotelDTO {

    private Long id;
    private String nombre;
    private String direccion;
    private String ciudad;
    private String pais;
    private String categoria;
    private String descripcion;
}