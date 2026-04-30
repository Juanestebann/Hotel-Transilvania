package com.example.ms_cliente_service.repository;

import com.example.ms_cliente_service.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    // Buscar cliente por rutDocumento
    Optional<Cliente> findByRutDocumento(String rutDocumento);

    // Obtener clientes por rolCliente
    List<Cliente> findByRolCliente(String rolCliente);

    // Obtener clientes por tipoCliente
    List<Cliente> findByTipoCliente(String tipoCliente);

}