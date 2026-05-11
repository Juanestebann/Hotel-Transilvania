package com.example.ms_pago_service.model;

import com.example.ms_pago_service.model.enums.EstadoPago;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pago")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idPago")
    private Long idPago;

    @NotNull(message = "El id de la reserva no puede ser nulo")
    private Long reservaId;

    @NotNull(message = "El id del usuario no puede ser nulo")
    private Long idUsuario;

    @NotNull(message = "El monto no puede ser nulo")
    private BigDecimal monto;

    @NotBlank(message = "El método de pago no puede estar vacío")
    private String metodoPago;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "El estado del pago no puede ser nulo")
    private EstadoPago estadoPago;

    @NotNull(message = "La fecha de pago no puede ser nula")
    private LocalDateTime fechaPago;
}