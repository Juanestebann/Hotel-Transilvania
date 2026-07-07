package com.example.ms_cliente_service.service;

import com.example.ms_cliente_service.dto.ClienteValidacionDTO;
import com.example.ms_cliente_service.model.Cliente;
import com.example.ms_cliente_service.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public List<Cliente> findAll() {

        log.info("Listando todos los clientes");

        return clienteRepository.findAll();
    }

    public Cliente findById(Long id) {

        log.info("Buscando cliente con id: {}", id);

        return clienteRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Cliente no encontrado con id: {}", id);
                    return new NoSuchElementException("Cliente no encontrado con id: " + id);
                });
    }

    public ClienteValidacionDTO validarExistenciaCliente(Long id) {
        log.info("Validando existencia de cliente con id: {}", id);

        if (!clienteRepository.existsById(id)) {
            log.error("Cliente no encontrado con id: {}", id);
            throw new NoSuchElementException("Cliente no encontrado con id: " + id);
        }

        return new ClienteValidacionDTO(id, true);
    }

    public Cliente findByRutDocumento(String rutDocumento) {

        log.info("Buscando cliente con rutDocumento: {}", rutDocumento);

        return clienteRepository.findByRutDocumento(rutDocumento)
                .orElseThrow(() -> {
                    log.error("Cliente no encontrado con rutDocumento: {}", rutDocumento);
                    return new NoSuchElementException("Cliente no encontrado con rutDocumento: " + rutDocumento);
                });
    }


    public List<Cliente> findByRolCliente(String rolCliente) {

        log.info("Filtrando clientes por rolCliente: {}", rolCliente);

        return clienteRepository.findByRolCliente(rolCliente);
    }

    public List<Cliente> findByTipoCliente(String tipoCliente) {

        log.info("Filtrando clientes por tipoCliente: {}", tipoCliente);

        return clienteRepository.findByTipoCliente(tipoCliente);
    }

    public Cliente guardar(Cliente cliente) {

        log.info("Creando cliente con rutDocumento: {}", cliente.getRutDocumento());

        Cliente clienteGuardado = clienteRepository.save(cliente);

        log.info("Cliente creado correctamente con id: {}", clienteGuardado.getId());

        return clienteGuardado;
    }

    public Cliente actualizar(Long id, Cliente clienteActualizado) {

        log.info("Actualizando cliente con id: {}", id);

        Cliente clienteExistente = findById(id);

        clienteExistente.setRutDocumento(clienteActualizado.getRutDocumento());
        clienteExistente.setTelefono(clienteActualizado.getTelefono());
        clienteExistente.setDireccion(clienteActualizado.getDireccion());
        clienteExistente.setRolCliente(clienteActualizado.getRolCliente());
        clienteExistente.setTipoCliente(clienteActualizado.getTipoCliente());

        Cliente clienteGuardado = clienteRepository.save(clienteExistente);

        log.info("Cliente actualizado correctamente con id: {}", id);

        return clienteGuardado;
    }

    public void eliminar(Long id) {

        log.warn("Solicitando eliminación de cliente con id: {}", id);

        Cliente cliente = findById(id);

        clienteRepository.delete(cliente);

        log.info("Cliente eliminado correctamente con id: {}", id);
    }
}
