package com.example.ms_notificacion_service.service;
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

    public List<Notificacion> findAll() {
        return notificacionRepository.findAll();
    }

    public Notificacion findById(Long id) {
        return notificacionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Notificación no encontrada con id: " + id));
    }

    public Notificacion guardar(Notificacion notificacion) {
        return notificacionRepository.save(notificacion);
    }

    public Notificacion actualizar(Long id, Notificacion notificacionActualizada) {
        Notificacion existente = findById(id);
        existente.setTipo(notificacionActualizada.getTipo());
        existente.setMensaje(notificacionActualizada.getMensaje());
        existente.setEstado(notificacionActualizada.getEstado());
        existente.setFechaEnvio(notificacionActualizada.getFechaEnvio());
        return notificacionRepository.save(existente);
    }

    public void eliminar(Long id) {
        Notificacion notificacion = findById(id);
        notificacionRepository.delete(notificacion);
    }
}