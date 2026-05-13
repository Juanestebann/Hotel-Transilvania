package com.example.ms_notificacion_service.service;

import com.example.ms_notificacion_service.client.ClienteClient;
import com.example.ms_notificacion_service.client.ReservaClient;
import com.example.ms_notificacion_service.client.UsuarioClient;
import com.example.ms_notificacion_service.model.Notificacion;
import com.example.ms_notificacion_service.repository.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final ClienteClient clienteClient;
    private final UsuarioClient usuarioClient;
    private final ReservaClient reservaClient;

    public List<Notificacion> findAll() {
        log.info("Listando notificaciones");
        return notificacionRepository.findAll();
    }

    public Notificacion findById(Long id) {
        log.info("Buscando notificación con id: {}", id);

        return notificacionRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Notificación no encontrada con id: {}", id);
                    return new NoSuchElementException("Notificación no encontrada con id: " + id);
                });
    }

    public List<Notificacion> findByIdCliente(Long idCliente) {
        log.info("Buscando notificaciones por idCliente: {}", idCliente);
        return notificacionRepository.findByIdCliente(idCliente);
    }

    public List<Notificacion> findByIdUsuario(Long idUsuario) {
        log.info("Buscando notificaciones por idUsuario: {}", idUsuario);
        return notificacionRepository.findByIdUsuario(idUsuario);
    }

    public List<Notificacion> findByIdReserva(Long idReserva) {
        log.info("Buscando notificaciones por idReserva: {}", idReserva);
        return notificacionRepository.findByIdReserva(idReserva);
    }

    public Notificacion guardar(Notificacion notificacion) {

        log.info("Registrando notificación tipo: {}", notificacion.getTipo());

        log.info(
                "Notificación asociada a cliente: {}, usuario: {}, reserva: {}",
                notificacion.getIdCliente(),
                notificacion.getIdUsuario(),
                notificacion.getIdReserva()
        );

        log.info("Validando cliente con id: {}", notificacion.getIdCliente());
        clienteClient.obtenerClientePorId(notificacion.getIdCliente());

        log.info("Validando usuario con id: {}", notificacion.getIdUsuario());
        usuarioClient.obtenerUsuarioPorId(notificacion.getIdUsuario());

        log.info("Validando reserva con id: {}", notificacion.getIdReserva());
        reservaClient.obtenerReservaPorId(notificacion.getIdReserva());

        Notificacion notificacionGuardada = notificacionRepository.save(notificacion);

        log.info("Notificación registrada correctamente con id: {}", notificacionGuardada.getId());

        return notificacionGuardada;
    }

    public Notificacion actualizar(Long id, Notificacion notificacionActualizada) {

        log.info("Actualizando notificación con id: {}", id);

        Notificacion existente = findById(id);

        log.info(
                "Nueva asociación de notificación a cliente: {}, usuario: {}, reserva: {}",
                notificacionActualizada.getIdCliente(),
                notificacionActualizada.getIdUsuario(),
                notificacionActualizada.getIdReserva()
        );

        log.info("Validando cliente con id: {}", notificacionActualizada.getIdCliente());
        clienteClient.obtenerClientePorId(notificacionActualizada.getIdCliente());

        log.info("Validando usuario con id: {}", notificacionActualizada.getIdUsuario());
        usuarioClient.obtenerUsuarioPorId(notificacionActualizada.getIdUsuario());

        log.info("Validando reserva con id: {}", notificacionActualizada.getIdReserva());
        reservaClient.obtenerReservaPorId(notificacionActualizada.getIdReserva());

        existente.setIdCliente(notificacionActualizada.getIdCliente());
        existente.setIdUsuario(notificacionActualizada.getIdUsuario());
        existente.setIdReserva(notificacionActualizada.getIdReserva());
        existente.setTipo(notificacionActualizada.getTipo());
        existente.setMensaje(notificacionActualizada.getMensaje());
        existente.setEstado(notificacionActualizada.getEstado());
        existente.setFechaEnvio(notificacionActualizada.getFechaEnvio());

        Notificacion notificacionGuardada = notificacionRepository.save(existente);

        log.info("Notificación actualizada correctamente con id: {}", notificacionGuardada.getId());

        return notificacionGuardada;
    }

    public void eliminar(Long id) {

        log.warn("Eliminando notificación con id: {}", id);

        Notificacion notificacion = findById(id);

        notificacionRepository.delete(notificacion);

        log.info("Notificación eliminada correctamente con id: {}", id);
    }
}