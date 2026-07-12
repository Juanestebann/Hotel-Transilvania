package com.example.ms_cliente_service.controller;

import com.example.ms_cliente_service.dto.ClienteValidacionDTO;
import com.example.ms_cliente_service.model.Cliente;
import com.example.ms_cliente_service.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Tag(
        name = "Clientes",
        description = "Operaciones protegidas para consultar, validar y administrar clientes"
)
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @Operation(
            summary = "Validar existencia de cliente",
            description = "Confirma si existe un cliente por ID para flujos que dependen de clientes. Requiere rol USER o ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente validado correctamente"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuario sin permisos para validar clientes", content = @Content),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado", content = @Content)
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{id}/validacion")
    public ResponseEntity<ClienteValidacionDTO> validarExistencia(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.validarExistenciaCliente(id));
    }

    @Operation(
            summary = "Listar clientes",
            description = "Lista todos los clientes o filtra por rolCliente o tipoCliente. Requiere rol ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Clientes listados correctamente"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuario sin permisos de ADMIN", content = @Content)
    })
    // Solo ADMIN puede listar clientes o filtrar por rol/tipo
    // http://localhost:8082/api/v1/clientes
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<?> findAll(
            @RequestParam(required = false) String rolCliente,
            @RequestParam(required = false) String tipoCliente
    ) {
        if (rolCliente != null) {
            List<Cliente> clientes = clienteService.findByRolCliente(rolCliente);
            clientes.forEach(this::agregarLinksCliente);
            return ResponseEntity.status(HttpStatus.OK).body(clientes);
        }

        if (tipoCliente != null) {
            List<Cliente> clientes = clienteService.findByTipoCliente(tipoCliente);
            clientes.forEach(this::agregarLinksCliente);
            return ResponseEntity.status(HttpStatus.OK).body(clientes);
        }

        List<Cliente> clientes = clienteService.findAll();
        clientes.forEach(this::agregarLinksCliente);
        return ResponseEntity.status(HttpStatus.OK).body(clientes);
    }

    @Operation(
            summary = "Buscar cliente por ID",
            description = "Obtiene un cliente específico por su identificador. Requiere rol ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuario sin permisos de ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado", content = @Content)
    })
    // Solo ADMIN puede buscar clientes por ID
    // http://localhost:8082/api/v1/clientes/1
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<Cliente> findById(@PathVariable Long id) {
        Cliente cliente = clienteService.findById(id);
        agregarLinksCliente(cliente);
        return ResponseEntity.status(HttpStatus.OK).body(cliente);
    }

    @Operation(
            summary = "Buscar cliente por RUT",
            description = "Obtiene un cliente por su rutDocumento. Requiere rol ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuario sin permisos de ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado", content = @Content)
    })
    // Solo ADMIN puede buscar clientes por RUT
    // http://localhost:8082/api/v1/clientes/rut/11111111-1
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/rut/{rutDocumento}")
    public ResponseEntity<Cliente> findByRutDocumento(@PathVariable String rutDocumento) {
        Cliente cliente = clienteService.findByRutDocumento(rutDocumento);
        agregarLinksCliente(cliente);
        return ResponseEntity.status(HttpStatus.OK).body(cliente);
    }

    @Operation(
            summary = "Crear cliente",
            description = "Registra un cliente con RUT, teléfono, dirección, rolCliente y tipoCliente. Requiere rol ADMIN.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = Cliente.class),
                            examples = @ExampleObject(
                                    name = "Cliente",
                                    value = """
                                            {
                                              "rutDocumento": "11111111-1",
                                              "telefono": "+56911112222",
                                              "direccion": "Av. Siempre Viva 742",
                                              "rolCliente": "USER",
                                              "tipoCliente": "TITULAR"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cliente creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o restricción de persistencia", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuario sin permisos de ADMIN", content = @Content)
    })
    // Solo ADMIN puede crear clientes
    // http://localhost:8082/api/v1/clientes
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Cliente> guardarCliente(@Valid @RequestBody Cliente cliente) {
        Cliente nuevoCliente = clienteService.guardar(cliente);
        agregarLinksCliente(nuevoCliente);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoCliente);
    }

    @Operation(
            summary = "Actualizar cliente",
            description = "Actualiza los datos de un cliente existente. Requiere rol ADMIN.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = Cliente.class),
                            examples = @ExampleObject(
                                    name = "Cliente actualizado",
                                    value = """
                                            {
                                              "rutDocumento": "11111111-1",
                                              "telefono": "+56922223333",
                                              "direccion": "Calle Nueva 123",
                                              "rolCliente": "USER",
                                              "tipoCliente": "TITULAR"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuario sin permisos de ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado", content = @Content)
    })
    // Solo ADMIN puede actualizar clientes
    // http://localhost:8082/api/v1/clientes/1
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Cliente> actualizarCliente(
            @PathVariable Long id,
            @Valid @RequestBody Cliente cliente
    ) {
        Cliente clienteActualizado = clienteService.actualizar(id, cliente);
        agregarLinksCliente(clienteActualizado);
        return ResponseEntity.status(HttpStatus.OK).body(clienteActualizado);
    }

    @Operation(
            summary = "Eliminar cliente",
            description = "Elimina un cliente por su identificador. Requiere rol ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cliente eliminado correctamente", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuario sin permisos de ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado", content = @Content)
    })
    // Solo ADMIN puede eliminar clientes
    // http://localhost:8082/api/v1/clientes/5
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCliente(@PathVariable Long id) {
        clienteService.eliminar(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    private void agregarLinksCliente(Cliente cliente) {
        cliente.add(linkTo(methodOn(ClienteController.class)
                .findById(cliente.getId())).withSelfRel());
        cliente.add(linkTo(methodOn(ClienteController.class)
                .findAll(null, null)).withRel("listar-todos"));
        cliente.add(linkTo(methodOn(ClienteController.class)
                .guardarCliente(null)).withRel("crear"));
        cliente.add(linkTo(methodOn(ClienteController.class)
                .actualizarCliente(cliente.getId(), null)).withRel("actualizar"));
        cliente.add(linkTo(methodOn(ClienteController.class)
                .eliminarCliente(cliente.getId())).withRel("eliminar"));
        cliente.add(linkTo(methodOn(ClienteController.class)
                .findByRutDocumento(cliente.getRutDocumento())).withRel("buscar-por-rut"));
        cliente.add(linkTo(methodOn(ClienteController.class)
                .findAll(cliente.getRolCliente(), null)).withRel("buscar-por-rol"));
        cliente.add(linkTo(methodOn(ClienteController.class)
                .findAll(null, cliente.getTipoCliente())).withRel("buscar-por-tipo"));
    }
}
