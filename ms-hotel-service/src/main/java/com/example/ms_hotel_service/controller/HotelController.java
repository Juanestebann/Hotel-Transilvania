package com.example.ms_hotel_service.controller;

import com.example.ms_hotel_service.model.Hotel;
import com.example.ms_hotel_service.service.HotelService;
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
        name = "Hoteles",
        description = "Operaciones de consulta pública y administración protegida de hoteles"
)
@RestController
@RequestMapping("/api/v1/hoteles")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;

    @Operation(
            summary = "Listar hoteles",
            description = "Obtiene todos los hoteles disponibles. Endpoint público de consulta."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Hoteles listados correctamente")
    })
    //http://localhost:8083/api/v1/hoteles
    @GetMapping
    public ResponseEntity<List<Hotel>> findAll() {
        List<Hotel> hoteles = hotelService.findAll();
        hoteles.forEach(this::agregarLinksHotel);
        return ResponseEntity.ok(hoteles);
    }

    @Operation(
            summary = "Buscar hotel por ID",
            description = "Obtiene un hotel específico por su identificador. Endpoint público de consulta."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Hotel encontrado"),
            @ApiResponse(responseCode = "404", description = "Hotel no encontrado", content = @Content)
    })
    //http://localhost:8083/api/v1/hoteles/1
    //http://localhost:8083/api/v1/hoteles/12312

    @GetMapping("/{id}")
    public ResponseEntity<Hotel> findById(@PathVariable Long id) {
        Hotel hotel = hotelService.findById(id);
        agregarLinksHotel(hotel);
        return ResponseEntity.ok(hotel);
    }

    @Operation(
            summary = "Buscar hoteles por categoría",
            description = "Lista hoteles filtrados por categoría, por ejemplo 5 estrellas. Endpoint público de consulta."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Hoteles filtrados correctamente")
    })
    //http://localhost:8083/api/v1/hoteles?categoria=5 estrellas
    @GetMapping(params = "categoria")
    public ResponseEntity<List<Hotel>> findByCategoria(@RequestParam String categoria) {
        List<Hotel> hoteles = hotelService.findByCategoria(categoria);
        hoteles.forEach(this::agregarLinksHotel);
        return ResponseEntity.ok(hoteles);
    }

    @Operation(
            summary = "Buscar hoteles por ciudad",
            description = "Lista hoteles filtrados por ciudad. Endpoint público de consulta."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Hoteles filtrados correctamente")
    })
    //http://localhost:8083/api/v1/hoteles?ciudad=Santiago
    @GetMapping(params = "ciudad")
    public ResponseEntity<List<Hotel>> findByCiudad(@RequestParam String ciudad) {
        List<Hotel> hoteles = hotelService.findByCiudad(ciudad);
        hoteles.forEach(this::agregarLinksHotel);
        return ResponseEntity.ok(hoteles);
    }

    @Operation(
            summary = "Buscar hoteles por país",
            description = "Lista hoteles filtrados por país. Endpoint público de consulta."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Hoteles filtrados correctamente")
    })
    //http://localhost:8083/api/v1/hoteles?pais=Chile
    @GetMapping(params = "pais")
    public ResponseEntity<List<Hotel>> findByPais(@RequestParam String pais) {
        List<Hotel> hoteles = hotelService.findByPais(pais);
        hoteles.forEach(this::agregarLinksHotel);
        return ResponseEntity.ok(hoteles);
    }

    @Operation(
            summary = "Crear hotel",
            description = "Registra un hotel con sus datos principales. Requiere rol ADMIN.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = Hotel.class),
                            examples = @ExampleObject(
                                    name = "Hotel",
                                    value = """
                                            {
                                              "nombre": "Hotel Transilvania Santiago",
                                              "direccion": "Av. Hotelera 100",
                                              "ciudad": "Santiago",
                                              "pais": "Chile",
                                              "categoria": "5 estrellas",
                                              "descripcion": "Hotel temático familiar con servicios premium"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Hotel creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuario sin permisos de ADMIN", content = @Content)
    })
    //http://localhost:8083/api/v1/hoteles
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Hotel> guardarHotel(@Valid @RequestBody Hotel hotel) {
        Hotel nuevoHotel = hotelService.guardar(hotel);
        agregarLinksHotel(nuevoHotel);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(nuevoHotel);
    }

    @Operation(
            summary = "Actualizar hotel",
            description = "Actualiza los datos de un hotel existente. Requiere rol ADMIN.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = Hotel.class),
                            examples = @ExampleObject(
                                    name = "Hotel actualizado",
                                    value = """
                                            {
                                              "nombre": "Hotel Transilvania Santiago",
                                              "direccion": "Av. Hotelera 200",
                                              "ciudad": "Santiago",
                                              "pais": "Chile",
                                              "categoria": "5 estrellas",
                                              "descripcion": "Hotel actualizado con spa y restaurant"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Hotel actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuario sin permisos de ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Hotel no encontrado", content = @Content)
    })
    //http://localhost:8083/api/v1/hoteles/1
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Hotel> actualizarHotel(
            @PathVariable Long id,
            @Valid @RequestBody Hotel hotel) {

        Hotel hotelActualizado = hotelService.actualizarHotel(id, hotel);
        agregarLinksHotel(hotelActualizado);
        return ResponseEntity.ok(hotelActualizado);
    }

    @Operation(
            summary = "Eliminar hotel",
            description = "Elimina un hotel por su identificador. Requiere rol ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Hotel eliminado correctamente", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuario sin permisos de ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Hotel no encontrado", content = @Content)
    })
    //http://localhost:8083/api/v1/hoteles/6
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarHotel(@PathVariable Long id) {
        hotelService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    private void agregarLinksHotel(Hotel hotel) {
        hotel.add(linkTo(methodOn(HotelController.class)
                .findById(hotel.getId())).withSelfRel());
        hotel.add(linkTo(methodOn(HotelController.class)
                .findAll()).withRel("listar-todos"));
        hotel.add(linkTo(methodOn(HotelController.class)
                .guardarHotel(null)).withRel("crear"));
        hotel.add(linkTo(methodOn(HotelController.class)
                .actualizarHotel(hotel.getId(), null)).withRel("actualizar"));
        hotel.add(linkTo(methodOn(HotelController.class)
                .eliminarHotel(hotel.getId())).withRel("eliminar"));
        hotel.add(linkTo(methodOn(HotelController.class)
                .findByCategoria(hotel.getCategoria())).withRel("buscar-por-categoria"));
        hotel.add(linkTo(methodOn(HotelController.class)
                .findByCiudad(hotel.getCiudad())).withRel("buscar-por-ciudad"));
        hotel.add(linkTo(methodOn(HotelController.class)
                .findByPais(hotel.getPais())).withRel("buscar-por-pais"));
    }
}
