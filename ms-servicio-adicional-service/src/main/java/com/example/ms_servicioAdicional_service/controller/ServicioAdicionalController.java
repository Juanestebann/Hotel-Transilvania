package com.example.ms_servicioAdicional_service.controller;

import com.example.ms_servicioAdicional_service.model.ServicioAdicionalModel;
import com.example.ms_servicioAdicional_service.service.ServicioAdicionalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/servicios-adicionales")
@RequiredArgsConstructor
public class ServicioAdicionalController {

    private final ServicioAdicionalService servicioAdicionalService;

    @GetMapping
    public ResponseEntity<?> findAll(
            @RequestParam(required = false) Long idHotel,
            @RequestParam(required = false) Long idReserva,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String nombre

    ) {
        //http://localhost:8090/api/v1/servicios-adicionales?idHotel=1
        if (idHotel != null) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(servicioAdicionalService.findByIdHotel(idHotel));
        }

        //http://localhost:8090/api/v1/servicios-adicionales?idReserva=1
        if (idReserva != null) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(servicioAdicionalService.findByIdReserva(idReserva));
        }

        //http://localhost:8090/api/v1/servicios-adicionales?estado=ACTIVO
        if (estado != null) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(servicioAdicionalService.findByEstado(estado));
        }
        //http://localhost:8090/api/v1/servicios-adicionales?nombre=SPA
        if (nombre != null) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(servicioAdicionalService.findByNombre(nombre));
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(servicioAdicionalService.findAll());
    }

    //http://localhost:8090/api/v1/servicios-adicionales/1
    @GetMapping("/{id}")
    public ResponseEntity<ServicioAdicionalModel> findById(@PathVariable Long id) {

        ServicioAdicionalModel servicio = servicioAdicionalService.findById(id);

        return ResponseEntity.status(HttpStatus.OK).body(servicio);
    }


    @PostMapping
    public ResponseEntity<ServicioAdicionalModel> guardarServicio(
            @Valid @RequestBody ServicioAdicionalModel servicioAdicional
    ) {

        ServicioAdicionalModel nuevoServicio =
                servicioAdicionalService.guardar(servicioAdicional);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(nuevoServicio);
    }


    @PutMapping("/{id}")
    public ResponseEntity<ServicioAdicionalModel> actualizarServicio(
            @PathVariable Long id,
            @Valid @RequestBody ServicioAdicionalModel servicioAdicional
    ) {

        ServicioAdicionalModel servicioActualizado =
                servicioAdicionalService.actualizar(id, servicioAdicional);

        return ResponseEntity.status(HttpStatus.OK)
                .body(servicioActualizado);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarServicio(@PathVariable Long id) {

        servicioAdicionalService.eliminar(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}