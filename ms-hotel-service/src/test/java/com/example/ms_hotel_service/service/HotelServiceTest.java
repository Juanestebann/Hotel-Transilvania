package com.example.ms_hotel_service.service;

import com.example.ms_hotel_service.model.Hotel;
import com.example.ms_hotel_service.repository.HotelRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class HotelServiceTest {

    @Mock
    private HotelRepository repository;

    @InjectMocks
    private HotelService service;

    @Test
    void deberiaListarHoteles() {
        List<Hotel> hoteles = List.of(crearHotel(1L), crearHotel(2L));
        Mockito.when(repository.findAll()).thenReturn(hoteles);

        List<Hotel> resultado = service.findAll();

        assertEquals(2, resultado.size());
        assertEquals("Hotel Transilvania 1", resultado.get(0).getNombre());
        verify(repository).findAll();
        verifyNoMoreInteractions(repository);
    }

    @Test
    void deberiaRetornarHotelCuandoExiste() {
        Hotel hotel = crearHotel(1L);
        Mockito.when(repository.findById(1L)).thenReturn(Optional.of(hotel));

        Hotel resultado = service.findById(1L);

        assertEquals(1L, resultado.getId());
        assertEquals("Hotel Transilvania 1", resultado.getNombre());
        assertTrue(resultado.getCategoria().contains("estrellas"));
        verify(repository).findById(1L);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void deberiaLanzarErrorCuandoHotelNoExiste() {
        Mockito.when(repository.findById(99L)).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> service.findById(99L)
        );

        assertEquals("Hotel no encontrado con id: 99", exception.getMessage());
        verify(repository).findById(99L);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void deberiaCrearHotelLimpiandoIdEntrante() {
        Hotel hotel = crearHotel(99L);
        Hotel guardado = crearHotel(1L);
        Mockito.when(repository.save(hotel)).thenReturn(guardado);

        Hotel resultado = service.guardar(hotel);

        assertNull(hotel.getId());
        assertEquals(1L, resultado.getId());
        assertEquals("Santiago", resultado.getCiudad());
        verify(repository).save(hotel);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void deberiaActualizarHotelExistente() {
        Hotel existente = crearHotel(1L);
        Hotel cambios = crearHotel(null);
        cambios.setNombre("Hotel Renovado");
        cambios.setDireccion("Av Nueva 456");
        cambios.setCiudad("Valparaiso");
        cambios.setPais("Chile");
        cambios.setCategoria("4 estrellas");
        cambios.setDescripcion("Descripcion actualizada");

        Mockito.when(repository.findById(1L)).thenReturn(Optional.of(existente));
        Mockito.when(repository.save(existente)).thenReturn(existente);

        Hotel resultado = service.actualizarHotel(1L, cambios);

        assertEquals(1L, resultado.getId());
        assertEquals("Hotel Renovado", resultado.getNombre());
        assertEquals("Valparaiso", resultado.getCiudad());
        assertEquals("4 estrellas", resultado.getCategoria());
        verify(repository).findById(1L);
        verify(repository).save(existente);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void actualizarHotelInexistenteNoGuardaCambios() {
        Mockito.when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> service.actualizarHotel(99L, crearHotel(null))
        );

        verify(repository).findById(99L);
        verify(repository, never()).save(Mockito.any());
        verifyNoMoreInteractions(repository);
    }

    @Test
    void deberiaEliminarHotelExistente() {
        Hotel hotel = crearHotel(1L);
        Mockito.when(repository.findById(1L)).thenReturn(Optional.of(hotel));

        service.eliminar(1L);

        verify(repository).findById(1L);
        verify(repository).delete(hotel);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void eliminarHotelInexistenteNoInvocaDelete() {
        Mockito.when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.eliminar(99L));

        verify(repository).findById(99L);
        verify(repository, never()).delete(Mockito.any());
        verifyNoMoreInteractions(repository);
    }

    @Test
    void deberiaFiltrarPorCategoria() {
        Mockito.when(repository.findByCategoria("5 estrellas"))
                .thenReturn(List.of(crearHotel(1L)));

        List<Hotel> resultado = service.findByCategoria("5 estrellas");

        assertEquals(1, resultado.size());
        assertEquals("5 estrellas", resultado.get(0).getCategoria());
        verify(repository).findByCategoria("5 estrellas");
        verifyNoMoreInteractions(repository);
    }

    @Test
    void deberiaFiltrarPorCiudad() {
        Mockito.when(repository.findByCiudad("Santiago"))
                .thenReturn(List.of(crearHotel(1L)));

        List<Hotel> resultado = service.findByCiudad("Santiago");

        assertEquals(1, resultado.size());
        assertEquals("Santiago", resultado.get(0).getCiudad());
        verify(repository).findByCiudad("Santiago");
        verifyNoMoreInteractions(repository);
    }

    @Test
    void deberiaFiltrarPorPais() {
        Mockito.when(repository.findByPais("Chile"))
                .thenReturn(List.of(crearHotel(1L), crearHotel(2L)));

        List<Hotel> resultado = service.findByPais("Chile");

        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(hotel -> "Chile".equals(hotel.getPais())));
        verify(repository).findByPais("Chile");
        verifyNoMoreInteractions(repository);
    }

    private Hotel crearHotel(Long id) {
        Hotel hotel = new Hotel();
        hotel.setId(id);
        hotel.setNombre("Hotel Transilvania " + id);
        hotel.setDireccion("Av Siempre Viva 123");
        hotel.setCiudad("Santiago");
        hotel.setPais("Chile");
        hotel.setCategoria("5 estrellas");
        hotel.setDescripcion("Hotel tematico");
        return hotel;
    }
}
