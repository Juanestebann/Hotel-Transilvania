package com.example.ms_notificacion_service.errorResponse;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;

@Data
public class ErrorResponse {
    private int status;
    private String error;
    private LocalDateTime timestamp;
    private HashMap<String, String> errores; // Almacena detalles como "campo": "mensaje"

    public ErrorResponse(int status, String error, HashMap<String, String> errores) {
        this.status = status;
        this.error = error;
        this.errores = errores;
        this.timestamp = LocalDateTime.now();
    }
}