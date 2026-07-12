package com.example.ms_cliente_service.service;

import com.example.ms_cliente_service.dto.ClienteValidacionDTO;
import com.example.ms_cliente_service.model.Cliente;
import com.example.ms_cliente_service.repository.ClienteRepository;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository repository;

    @InjectMocks
    private ClienteService service;

    @Test
    void deberiaListarClientes() {
        Mockito.when(repository.findAll())
                .thenReturn(List.of(crearCliente(1L), crearCliente(2L)));

        List<Cliente> resultado = service.findAll();

        assertEquals(2, resultado.size());
        assertEquals("11111111-1", resultado.get(0).getRutDocumento());
        verify(repository).findAll();
        verifyNoMoreInteractions(repository);
    }

    @Test
    void deberiaRetornarClienteCuandoExiste() {
        Cliente cliente = crearCliente(1L);
        Mockito.when(repository.findById(1L)).thenReturn(Optional.of(cliente));

        Cliente resultado = service.findById(1L);

        assertEquals(1L, resultado.getId());
        assertEquals("11111111-1", resultado.getRutDocumento());
        assertTrue(resultado.getTelefono().contains("+569"));
        verify(repository).findById(1L);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void buscarClienteInexistenteLanzaNotFound() {
        Mockito.when(repository.findById(99L)).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> service.findById(99L)
        );

        assertEquals("Cliente no encontrado con id: 99", exception.getMessage());
        verify(repository).findById(99L);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void deberiaBuscarPorRutDocumento() {
        Cliente cliente = crearCliente(1L);
        Mockito.when(repository.findByRutDocumento("11111111-1"))
                .thenReturn(Optional.of(cliente));

        Cliente resultado = service.findByRutDocumento("11111111-1");

        assertEquals(1L, resultado.getId());
        assertEquals("11111111-1", resultado.getRutDocumento());
        verify(repository).findByRutDocumento("11111111-1");
        verifyNoMoreInteractions(repository);
    }

    @Test
    void buscarPorRutInexistenteLanzaNotFound() {
        Mockito.when(repository.findByRutDocumento("99999999-9"))
                .thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> service.findByRutDocumento("99999999-9")
        );

        assertEquals("Cliente no encontrado con rutDocumento: 99999999-9", exception.getMessage());
        verify(repository).findByRutDocumento("99999999-9");
        verifyNoMoreInteractions(repository);
    }

    @Test
    void deberiaCrearCliente() {
        Cliente cliente = crearCliente(1L);
        Mockito.when(repository.save(cliente)).thenReturn(cliente);

        Cliente resultado = service.guardar(cliente);

        assertEquals(1L, resultado.getId());
        assertEquals("ADMIN", resultado.getRolCliente());
        verify(repository).save(cliente);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void deberiaActualizarClienteExistente() {
        Cliente existente = crearCliente(1L);
        Cliente cambios = crearCliente(null);
        cambios.setRutDocumento("22222222-2");
        cambios.setTelefono("+56922223333");
        cambios.setDireccion("Calle Nueva 123");
        cambios.setRolCliente("USER");
        cambios.setTipoCliente("TITULAR");

        Mockito.when(repository.findById(1L)).thenReturn(Optional.of(existente));
        Mockito.when(repository.save(existente)).thenReturn(existente);

        Cliente resultado = service.actualizar(1L, cambios);

        assertEquals("22222222-2", resultado.getRutDocumento());
        assertEquals("USER", resultado.getRolCliente());
        assertEquals("TITULAR", resultado.getTipoCliente());
        verify(repository).findById(1L);
        verify(repository).save(existente);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void actualizarClienteInexistenteNoGuarda() {
        Mockito.when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.actualizar(99L, crearCliente(null)));

        verify(repository).findById(99L);
        verify(repository, never()).save(Mockito.any());
        verifyNoMoreInteractions(repository);
    }

    @Test
    void deberiaEliminarClienteExistente() {
        Cliente cliente = crearCliente(1L);
        Mockito.when(repository.findById(1L)).thenReturn(Optional.of(cliente));

        service.eliminar(1L);

        verify(repository).findById(1L);
        verify(repository).delete(cliente);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void eliminarClienteInexistenteNoInvocaDelete() {
        Mockito.when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.eliminar(99L));

        verify(repository).findById(99L);
        verify(repository, never()).delete(Mockito.any());
        verifyNoMoreInteractions(repository);
    }

    @Test
    void deberiaValidarExistenciaSinRetornarDatosSensibles() {
        Mockito.when(repository.existsById(1L)).thenReturn(true);

        ClienteValidacionDTO resultado = service.validarExistenciaCliente(1L);

        assertEquals(1L, resultado.idCliente());
        assertTrue(resultado.existe());
        verify(repository).existsById(1L);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void deberiaRetornarNotFoundCuandoClienteNoExisteEnValidacion() {
        Mockito.when(repository.existsById(99L)).thenReturn(false);

        assertThrows(NoSuchElementException.class, () -> service.validarExistenciaCliente(99L));

        verify(repository).existsById(99L);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void deberiaFiltrarPorRolCliente() {
        Mockito.when(repository.findByRolCliente("ADMIN"))
                .thenReturn(List.of(crearCliente(1L)));

        List<Cliente> resultado = service.findByRolCliente("ADMIN");

        assertEquals(1, resultado.size());
        assertEquals("ADMIN", resultado.get(0).getRolCliente());
        verify(repository).findByRolCliente("ADMIN");
        verifyNoMoreInteractions(repository);
    }

    @Test
    void deberiaFiltrarPorTipoCliente() {
        Mockito.when(repository.findByTipoCliente("FRECUENTE"))
                .thenReturn(List.of(crearCliente(1L), crearCliente(2L)));

        List<Cliente> resultado = service.findByTipoCliente("FRECUENTE");

        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(cliente -> "FRECUENTE".equals(cliente.getTipoCliente())));
        verify(repository).findByTipoCliente("FRECUENTE");
        verifyNoMoreInteractions(repository);
    }

    private Cliente crearCliente(Long id) {
        Cliente cliente = new Cliente();
        cliente.setId(id);
        cliente.setRutDocumento("11111111-1");
        cliente.setTelefono("+56912345678");
        cliente.setDireccion("Santiago");
        cliente.setRolCliente("ADMIN");
        cliente.setTipoCliente("FRECUENTE");
        return cliente;
    }
}
