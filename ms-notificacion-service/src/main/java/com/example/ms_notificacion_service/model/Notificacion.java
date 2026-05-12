package com.example.ms_notificacion_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "notificacion")
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idNotificacion")
    private Long id;

    @NotNull(message = "El idCliente es obligatorio")
    @Column(name = "id_cliente", nullable = false)
    private Long idCliente;

    @NotNull(message = "El idUsuario es obligatorio")
    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @NotNull(message = "El idReserva es obligatorio")
    @Column(name = "id_reserva", nullable = false)
    private Long idReserva;

    @NotBlank(message = "El tipo de notificación es obligatorio")
    @Column(nullable = false)
    private String tipo;

    @NotBlank(message = "El mensaje es obligatorio")
    @Column(nullable = false)
    private String mensaje;

    @NotBlank(message = "El estado es obligatorio")
    @Column(nullable = false)
    private String estado;

    @Column(name = "fecha_envio")
    @NotNull(message = "La fecha de envío es obligatoria")
    private LocalDateTime fechaEnvio;
}