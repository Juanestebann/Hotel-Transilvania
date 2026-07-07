package com.example.ms_disponibilidad_service.dto;

import jakarta.validation.constraints.NotBlank;

public record DisponibilidadEstadoDTO(
        @NotBlank(message = "El estado es obligatorio") String estado
) {
}
