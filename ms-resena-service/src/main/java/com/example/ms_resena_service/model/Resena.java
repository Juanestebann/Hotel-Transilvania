package com.example.ms_resena_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "resena")
public class Resena {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_resena")
    private Long id;

    @NotNull(message = "El idCliente es obligatorio")
    @Column(name = "id_cliente", nullable = false)
    private Long idCliente;

    @NotNull(message = "El idHotel es obligatorio")
    @Column(name = "id_hotel", nullable = false)
    private Long idHotel;

    @NotNull(message = "El idHabitacion es obligatorio")
    @Column(name = "id_habitacion", nullable = false)
    private Long idHabitacion;

    @NotNull(message = "El idReserva es obligatorio")
    @Column(name = "id_reserva", nullable = false)
    private Long idReserva;

    @NotNull(message = "La calificación es obligatoria")
    @Min(value = 1, message = "Mínimo 1")
    @Max(value = 5, message = "Máximo 5")
    @Column(nullable = false)
    private Integer calificacion;

    @NotBlank(message = "El comentario es obligatorio")
    @Column(nullable = false, length = 500)
    private String comentario;

    @Column(name = "fecha_comentario")
    private LocalDate fechaComentario;

    @NotBlank(message = "El estado de la reseña es obligatorio")
    @Column(name = "estado_resena", nullable = false)
    private String estadoResena;

    @PrePersist
    public void asignarFecha() {
        this.fechaComentario = LocalDate.now();
    }
}