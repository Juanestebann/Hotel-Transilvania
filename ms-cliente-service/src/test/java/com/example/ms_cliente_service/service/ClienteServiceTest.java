package com.example.ms_cliente_service.service;

import com.example.ms_cliente_service.model.Cliente;
import com.example.ms_cliente_service.repository.ClienteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository repository;

    @InjectMocks
    private ClienteService service;

    @Test
    void deberiaRetornarClienteCuandoExiste() {

        Cliente cliente = crearCliente();

        Mockito.when(repository.findById(1L))
                .thenReturn(Optional.of(cliente));

        Cliente resultado = service.findById(1L);

        assertEquals(1L, resultado.getId());
        assertEquals("11111111-1", resultado.getRutDocumento());
        assertTrue(resultado.getTelefono().contains("+569"));

        verify(repository).findById(1L);
    }

    @Test
    void deberiaCrearCliente() {

        Cliente cliente = crearCliente();

        Mockito.when(repository.save(cliente))
                .thenReturn(cliente);

        Cliente resultado = service.guardar(cliente);

        assertEquals(1L, resultado.getId());
        assertEquals("ADMIN", resultado.getRolCliente());

        verify(repository).save(cliente);
    }

    private Cliente crearCliente() {
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setRutDocumento("11111111-1");
        cliente.setTelefono("+56912345678");
        cliente.setDireccion("Santiago");
        cliente.setRolCliente("ADMIN");
        cliente.setTipoCliente("FRECUENTE");
        return cliente;
    }
}
