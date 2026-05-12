package com.example.ms_notificacion_service.service;

import com.example.ms_notificacion_service.client.ClienteClient;
import com.example.ms_notificacion_service.client.ReservaClient;
import com.example.ms_notificacion_service.client.UsuarioClient;
import com.example.ms_notificacion_service.model.Notificacion;
import com.example.ms_notificacion_service.repository.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final ClienteClient clienteClient;
    private final UsuarioClient usuarioClient;
    private final ReservaClient reservaClient;

    // Obtener todas las notificaciones
    public List<Notificacion> findAll() {
        return notificacionRepository.findAll();
    }

    // Obtener notificación por id
    public Notificacion findById(Long id) {
        return notificacionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Notificación no encontrada con id: " + id));
    }

    // Obtener notificaciones por idCliente
    public List<Notificacion> findByIdCliente(Long idCliente) {
        return notificacionRepository.findByIdCliente(idCliente);
    }

    // Obtener notificaciones por idUsuario
    public List<Notificacion> findByIdUsuario(Long idUsuario) {
        return notificacionRepository.findByIdUsuario(idUsuario);
    }

    // Obtener notificaciones por idReserva
    public List<Notificacion> findByIdReserva(Long idReserva) {
        return notificacionRepository.findByIdReserva(idReserva);
    }

    // Guardar notificación validando cliente, usuario y reserva
    public Notificacion guardar(Notificacion notificacion) {

        // Validar cliente existente
        clienteClient.obtenerClientePorId(notificacion.getIdCliente());

        // Validar usuario existente
        usuarioClient.obtenerUsuarioPorId(notificacion.getIdUsuario());

        // Validar reserva existente
        reservaClient.obtenerReservaPorId(notificacion.getIdReserva());

        return notificacionRepository.save(notificacion);
    }

    // Actualizar notificación validando cliente, usuario y reserva
    public Notificacion actualizar(Long id, Notificacion notificacionActualizada) {
        Notificacion existente = findById(id);

        // Validar cliente existente
        clienteClient.obtenerClientePorId(notificacionActualizada.getIdCliente());

        // Validar usuario existente
        usuarioClient.obtenerUsuarioPorId(notificacionActualizada.getIdUsuario());

        // Validar reserva existente
        reservaClient.obtenerReservaPorId(notificacionActualizada.getIdReserva());

        existente.setIdCliente(notificacionActualizada.getIdCliente());
        existente.setIdUsuario(notificacionActualizada.getIdUsuario());
        existente.setIdReserva(notificacionActualizada.getIdReserva());
        existente.setTipo(notificacionActualizada.getTipo());
        existente.setMensaje(notificacionActualizada.getMensaje());
        existente.setEstado(notificacionActualizada.getEstado());
        existente.setFechaEnvio(notificacionActualizada.getFechaEnvio());

        return notificacionRepository.save(existente);
    }

    // Eliminar notificación por id
    public void eliminar(Long id) {
        Notificacion notificacion = findById(id);
        notificacionRepository.delete(notificacion);
    }
}