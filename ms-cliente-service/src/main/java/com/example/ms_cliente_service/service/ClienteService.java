package com.example.ms_cliente_service.service;

import com.example.ms_cliente_service.model.Cliente;
import com.example.ms_cliente_service.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public List<Cliente> findAll() {
        return clienteRepository.findAll();
    }

    public Cliente findById(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Cliente no encontrado con id: " + id));
    }

    public Cliente findByRutDocumento(String rutDocumento) {
        return clienteRepository.findByRutDocumento(rutDocumento)
                .orElseThrow(() -> new NoSuchElementException("Cliente no encontrado con rutDocumento: " + rutDocumento));
    }

    public List<Cliente> findByRolCliente(String rolCliente) {
        return clienteRepository.findByRolCliente(rolCliente);
    }

    public List<Cliente> findByTipoCliente(String tipoCliente) {
        return clienteRepository.findByTipoCliente(tipoCliente);
    }

    public Cliente guardar(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    public Cliente actualizar(Long id, Cliente clienteActualizado) {
        Cliente clienteExistente = findById(id);

        clienteExistente.setRutDocumento(clienteActualizado.getRutDocumento());
        clienteExistente.setTelefono(clienteActualizado.getTelefono());
        clienteExistente.setDireccion(clienteActualizado.getDireccion());
        clienteExistente.setRolCliente(clienteActualizado.getRolCliente());
        clienteExistente.setTipoCliente(clienteActualizado.getTipoCliente());

        return clienteRepository.save(clienteExistente);
    }

    public void eliminar(Long id) {
        Cliente cliente = findById(id);
        clienteRepository.delete(cliente);
    }
}