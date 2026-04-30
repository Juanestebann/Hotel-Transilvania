package com.example.ms_cliente_service.controller;

import com.example.ms_cliente_service.model.Cliente;
import com.example.ms_cliente_service.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @GetMapping
    public ResponseEntity<?> findAll(
            @RequestParam(required = false) String rolCliente,
            @RequestParam(required = false) String tipoCliente
    ) {
        if (rolCliente != null) {
            return ResponseEntity.status(HttpStatus.OK).body(clienteService.findByRolCliente(rolCliente));
        }

        if (tipoCliente != null) {
            return ResponseEntity.status(HttpStatus.OK).body(clienteService.findByTipoCliente(tipoCliente));
        }

        return ResponseEntity.status(HttpStatus.OK).body(clienteService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cliente> findById(@PathVariable Long id) {
        Cliente cliente = clienteService.findById(id);
        return ResponseEntity.status(HttpStatus.OK).body(cliente);
    }

    @GetMapping("/rut/{rutDocumento}")
    public ResponseEntity<Cliente> findByRutDocumento(@PathVariable String rutDocumento) {
        Cliente cliente = clienteService.findByRutDocumento(rutDocumento);
        return ResponseEntity.status(HttpStatus.OK).body(cliente);
    }

    @PostMapping
    public ResponseEntity<Cliente> guardarCliente(@Valid @RequestBody Cliente cliente) {
        Cliente nuevoCliente = clienteService.guardar(cliente);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoCliente);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cliente> actualizarCliente(
            @PathVariable Long id,
            @Valid @RequestBody Cliente cliente
    ) {
        Cliente clienteActualizado = clienteService.actualizar(id, cliente);
        return ResponseEntity.status(HttpStatus.OK).body(clienteActualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCliente(@PathVariable Long id) {
        clienteService.eliminar(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}