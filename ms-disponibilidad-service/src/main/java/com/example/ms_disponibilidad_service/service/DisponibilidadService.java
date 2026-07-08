package com.example.ms_disponibilidad_service.service;

import com.example.ms_disponibilidad_service.client.HabitacionClient;
import com.example.ms_disponibilidad_service.model.Disponibilidad;
import com.example.ms_disponibilidad_service.repository.DisponibilidadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class DisponibilidadService {

    private final DisponibilidadRepository disponibilidadRepository;
    private final HabitacionClient habitacionClient;

    public List<Disponibilidad> findAll() {
        log.info("Listando disponibilidad");
        return disponibilidadRepository.findAll();
    }

    public Disponibilidad findById(Long id) {
        log.info("Buscando disponibilidad con id: {}", id);

        return disponibilidadRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Disponibilidad no encontrada con id: {}", id);
                    return new NoSuchElementException("Disponibilidad no encontrada con id: " + id);
                });
    }

    public Disponibilidad findByIdHabitacionAndFecha(Long idHabitacion, LocalDate fecha) {
        log.info("Buscando disponibilidad para habitación id: {} en fecha: {}", idHabitacion, fecha);

        return disponibilidadRepository.findByIdHabitacionAndFecha(idHabitacion, fecha)
                .orElseThrow(() -> {
                    log.error("No existe disponibilidad para la habitación {} en la fecha {}", idHabitacion, fecha);
                    return new NoSuchElementException(
                            "No existe disponibilidad para la habitación " + idHabitacion + " en la fecha " + fecha
                    );
                });
    }

    public Disponibilidad guardar(Disponibilidad disponibilidad) {

        log.info("Creando disponibilidad para fecha: {}", disponibilidad.getFecha());

        validarEstado(disponibilidad.getEstado());

        log.info("Validando disponibilidad para habitación id: {}", disponibilidad.getIdHabitacion());

        habitacionClient.obtenerHabitacionPorId(disponibilidad.getIdHabitacion());

        validarDisponibilidadDuplicada(
                disponibilidad.getIdHabitacion(),
                disponibilidad.getFecha(),
                null
        );

        disponibilidad.setEstado(disponibilidad.getEstado().toUpperCase());

        Disponibilidad disponibilidadGuardada = disponibilidadRepository.save(disponibilidad);

        log.info("Disponibilidad creada correctamente con id: {}", disponibilidadGuardada.getId());

        return disponibilidadGuardada;
    }

    public Disponibilidad actualizar(Long id, Disponibilidad disponibilidadActualizada) {

        log.info("Actualizando disponibilidad con id: {}", id);

        validarEstado(disponibilidadActualizada.getEstado());

        log.info("Validando disponibilidad para habitación id: {}", disponibilidadActualizada.getIdHabitacion());

        habitacionClient.obtenerHabitacionPorId(disponibilidadActualizada.getIdHabitacion());

        validarDisponibilidadDuplicada(
                disponibilidadActualizada.getIdHabitacion(),
                disponibilidadActualizada.getFecha(),
                id
        );

        Disponibilidad existente = findById(id);

        existente.setIdHabitacion(disponibilidadActualizada.getIdHabitacion());
        existente.setFecha(disponibilidadActualizada.getFecha());
        existente.setEstado(disponibilidadActualizada.getEstado().toUpperCase());

        Disponibilidad disponibilidadGuardada = disponibilidadRepository.save(existente);

        log.info("Disponibilidad actualizada correctamente con id: {}", disponibilidadGuardada.getId());

        return disponibilidadGuardada;
    }

    public Disponibilidad actualizarEstadoInterno(Long id, String estado) {
        log.info("Actualizando internamente el estado de disponibilidad id: {}", id);

        validarEstadoInterno(estado);

        Disponibilidad existente = findById(id);
        String estadoActual = existente.getEstado().toUpperCase();
        String nuevoEstado = estado.toUpperCase();

        if (estadoActual.equals(nuevoEstado)) {
            log.info("Disponibilidad id: {} ya se encuentra en estado {}", id, nuevoEstado);
            return existente;
        }

        boolean transicionValida = ("DISPONIBLE".equals(estadoActual)
                && "OCUPADA".equals(nuevoEstado))
                || ("OCUPADA".equals(estadoActual)
                && "DISPONIBLE".equals(nuevoEstado));

        if (!transicionValida) {
            throw new IllegalArgumentException(
                    "Transición de disponibilidad inválida: "
                            + estadoActual + " -> " + nuevoEstado
            );
        }

        existente.setEstado(nuevoEstado);

        Disponibilidad disponibilidadGuardada = disponibilidadRepository.save(existente);

        log.info("Estado de disponibilidad actualizado internamente para id: {}", id);
        return disponibilidadGuardada;
    }

    private void validarEstadoInterno(String estado) {
        if (!"DISPONIBLE".equalsIgnoreCase(estado)
                && !"OCUPADA".equalsIgnoreCase(estado)) {
            throw new IllegalArgumentException(
                    "El flujo interno de reserva solo permite DISPONIBLE u OCUPADA"
            );
        }
    }

    public void eliminar(Long id) {

        log.warn("Eliminando disponibilidad con id: {}", id);

        Disponibilidad disponibilidad = findById(id);

        disponibilidadRepository.delete(disponibilidad);

        log.info("Disponibilidad eliminada correctamente con id: {}", id);
    }

    private void validarEstado(String estado) {

        log.info("Validando estado de disponibilidad: {}", estado);

        if (!estado.equalsIgnoreCase("DISPONIBLE") &&
                !estado.equalsIgnoreCase("OCUPADA") &&
                !estado.equalsIgnoreCase("MANTENIMIENTO")) {

            log.error("Estado de disponibilidad inválido recibido: {}", estado);

            throw new IllegalArgumentException(
                    "Estado inválido. Use: DISPONIBLE, OCUPADA o MANTENIMIENTO"
            );
        }
    }

    private void validarDisponibilidadDuplicada(
            Long idHabitacion,
            LocalDate fecha,
            Long idDisponibilidad
    ) {

        log.info("Validando duplicidad de disponibilidad para habitación id: {} en fecha: {}", idHabitacion, fecha);

        disponibilidadRepository
                .findByIdHabitacionAndFecha(idHabitacion, fecha)
                .ifPresent(disponibilidad -> {

                    if (idDisponibilidad == null ||
                            !disponibilidad.getId().equals(idDisponibilidad)) {

                        log.warn("Disponibilidad duplicada detectada para habitación id: {} en fecha: {}", idHabitacion, fecha);

                        throw new IllegalArgumentException(
                                "Ya existe disponibilidad para esta habitación en esa fecha"
                        );
                    }
                });
    }
}
