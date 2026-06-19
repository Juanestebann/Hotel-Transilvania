package com.example.ms_cliente_service.model;

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
@Table(name = "cliente")
public class Cliente extends RepresentationModel<Cliente> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCliente")
    private Long id;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "El rutDocumento es obligatorio")
    private String rutDocumento;

    @Column(nullable = false)
    @NotBlank(message = "El telefono es obligatorio")
    private String telefono;

    @Column(nullable = false)
    @NotBlank(message = "La direccion es obligatoria")
    private String direccion;

    @Column(nullable = false)
    @NotBlank(message = "El rolCliente es obligatorio")
    private String rolCliente;

    @Column(nullable = false)
    @NotBlank(message = "El tipoCliente es obligatorio")
    private String tipoCliente;

}
