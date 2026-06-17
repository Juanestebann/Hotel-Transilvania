package com.example.ms_servicioAdicional_service.service;

import com.example.ms_servicioAdicional_service.client.HotelClient;
import com.example.ms_servicioAdicional_service.client.ReservaClient;
import com.example.ms_servicioAdicional_service.model.ServicioAdicionalModel;
import com.example.ms_servicioAdicional_service.repository.ServicioAdicionalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicioAdicionalServiceTest {

    @Mock
    private ServicioAdicionalRepository servicioAdicionalRepository;

    @Mock
    private HotelClient hotelClient;

    @Mock
    private ReservaClient reservaClient;

    @InjectMocks
    private ServicioAdicionalService servicioAdicionalService;

    private ServicioAdicionalModel servicio;

    @BeforeEach
    void setUp() {
        servicio = new ServicioAdicionalModel();
        servicio.setId(1L);
        servicio.setIdHotel(1L);
        servicio.setIdReserva(1L);
        servicio.setNombre("SPA");
        servicio.setDescripcion("Servicio de spa y relajación");
        servicio.setPrecio(new BigDecimal("25000"));
        servicio.setEstado("ACTIVO");
    }

    @Test
    @DisplayName("Debe listar todos los servicios adicionales")
    void debeListarTodosLosServiciosAdicionales() {
        // Given
        when(servicioAdicionalRepository.findAll()).thenReturn(List.of(servicio));

        // When
        List<ServicioAdicionalModel> resultado = servicioAdicionalService.findAll();

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("SPA", resultado.get(0).getNombre());

        verify(servicioAdicionalRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe buscar un servicio adicional por ID cuando existe")
    void debeBuscarServicioAdicionalPorIdCuandoExiste() {
        // Given
        when(servicioAdicionalRepository.findById(1L)).thenReturn(Optional.of(servicio));

        // When
        ServicioAdicionalModel resultado = servicioAdicionalService.findById(1L);

        // Then
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("SPA", resultado.getNombre());
        assertEquals("ACTIVO", resultado.getEstado());

        verify(servicioAdicionalRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el servicio adicional no existe")
    void debeLanzarExcepcionCuandoServicioAdicionalNoExiste() {
        // Given
        when(servicioAdicionalRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> servicioAdicionalService.findById(99L)
        );

        assertEquals("Servicio adicional no encontrado con id: 99", exception.getMessage());

        verify(servicioAdicionalRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("Debe guardar un servicio adicional validando hotel y reserva")
    void debeGuardarServicioAdicionalValidandoHotelYReserva() {
        // Given
        when(servicioAdicionalRepository.save(servicio)).thenReturn(servicio);

        // When
        ServicioAdicionalModel resultado = servicioAdicionalService.guardar(servicio);

        // Then
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("SPA", resultado.getNombre());

        verify(hotelClient, times(1)).obtenerHotelPorId(1L);
        verify(reservaClient, times(1)).obtenerReservaPorId(1L);
        verify(servicioAdicionalRepository, times(1)).save(servicio);
    }

    @Test
    @DisplayName("Debe guardar un servicio adicional sin reserva cuando idReserva es null")
    void debeGuardarServicioAdicionalSinReserva() {
        // Given
        servicio.setIdReserva(null);

        when(servicioAdicionalRepository.save(servicio)).thenReturn(servicio);

        // When
        ServicioAdicionalModel resultado = servicioAdicionalService.guardar(servicio);

        // Then
        assertNotNull(resultado);
        assertNull(resultado.getIdReserva());
        assertEquals("SPA", resultado.getNombre());

        verify(hotelClient, times(1)).obtenerHotelPorId(1L);
        verify(reservaClient, never()).obtenerReservaPorId(anyLong());
        verify(servicioAdicionalRepository, times(1)).save(servicio);
    }

    @Test
    @DisplayName("Debe actualizar un servicio adicional existente")
    void debeActualizarServicioAdicionalExistente() {
        // Given
        ServicioAdicionalModel datosActualizados = new ServicioAdicionalModel();
        datosActualizados.setIdHotel(2L);
        datosActualizados.setIdReserva(3L);
        datosActualizados.setNombre("Desayuno");
        datosActualizados.setDescripcion("Desayuno buffet");
        datosActualizados.setPrecio(new BigDecimal("12000"));
        datosActualizados.setEstado("ACTIVO");

        when(servicioAdicionalRepository.findById(1L)).thenReturn(Optional.of(servicio));
        when(servicioAdicionalRepository.save(servicio)).thenReturn(servicio);

        // When
        ServicioAdicionalModel resultado =
                servicioAdicionalService.actualizar(1L, datosActualizados);

        // Then
        assertNotNull(resultado);
        assertEquals(2L, resultado.getIdHotel());
        assertEquals(3L, resultado.getIdReserva());
        assertEquals("Desayuno", resultado.getNombre());
        assertEquals("Desayuno buffet", resultado.getDescripcion());
        assertEquals(new BigDecimal("12000"), resultado.getPrecio());

        verify(servicioAdicionalRepository, times(1)).findById(1L);
        verify(hotelClient, times(1)).obtenerHotelPorId(2L);
        verify(reservaClient, times(1)).obtenerReservaPorId(3L);
        verify(servicioAdicionalRepository, times(1)).save(servicio);
    }

    @Test
    @DisplayName("Debe eliminar un servicio adicional existente")
    void debeEliminarServicioAdicionalExistente() {
        // Given
        when(servicioAdicionalRepository.findById(1L)).thenReturn(Optional.of(servicio));

        // When
        servicioAdicionalService.eliminar(1L);

        // Then
        verify(servicioAdicionalRepository, times(1)).findById(1L);
        verify(servicioAdicionalRepository, times(1)).delete(servicio);
    }

    @Test
    @DisplayName("Debe buscar servicios adicionales por hotel")
    void debeBuscarServiciosPorHotel() {
        // Given
        when(servicioAdicionalRepository.findByIdHotel(1L)).thenReturn(List.of(servicio));

        // When
        List<ServicioAdicionalModel> resultado = servicioAdicionalService.findByIdHotel(1L);

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());

        verify(servicioAdicionalRepository, times(1)).findByIdHotel(1L);
    }

    @Test
    @DisplayName("Debe buscar servicios adicionales por estado")
    void debeBuscarServiciosPorEstado() {
        // Given
        when(servicioAdicionalRepository.findByEstado("ACTIVO")).thenReturn(List.of(servicio));

        // When
        List<ServicioAdicionalModel> resultado = servicioAdicionalService.findByEstado("ACTIVO");

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("ACTIVO", resultado.get(0).getEstado());

        verify(servicioAdicionalRepository, times(1)).findByEstado("ACTIVO");
    }
}