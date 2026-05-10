package com.example.ms_disponibilidad_service.service;

import com.example.ms_disponibilidad_service.client.HabitacionClient;
import com.example.ms_disponibilidad_service.model.Disponibilidad;
import com.example.ms_disponibilidad_service.repository.DisponibilidadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class DisponibilidadService {

    private final DisponibilidadRepository disponibilidadRepository;
    private final HabitacionClient habitacionClient;

    public List<Disponibilidad> findAll() {
        return disponibilidadRepository.findAll();
    }

    public Disponibilidad findById(Long id) {
        return disponibilidadRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Disponibilidad no encontrada con id: " + id));
    }

    public Disponibilidad guardar(Disponibilidad disponibilidad) {

        validarEstado(disponibilidad.getEstado());

        habitacionClient.obtenerHabitacionPorId(disponibilidad.getIdHabitacion());

        validarDisponibilidadDuplicada(
                disponibilidad.getIdHabitacion(),
                disponibilidad.getFecha(),
                null
        );

        disponibilidad.setEstado(disponibilidad.getEstado().toUpperCase());

        return disponibilidadRepository.save(disponibilidad);
    }

    public Disponibilidad actualizar(Long id, Disponibilidad disponibilidadActualizada) {

        validarEstado(disponibilidadActualizada.getEstado());

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

        return disponibilidadRepository.save(existente);
    }

    public void eliminar(Long id) {
        Disponibilidad disponibilidad = findById(id);
        disponibilidadRepository.delete(disponibilidad);
    }

    private void validarEstado(String estado) {

        if (!estado.equalsIgnoreCase("DISPONIBLE") &&
                !estado.equalsIgnoreCase("OCUPADA") &&
                !estado.equalsIgnoreCase("MANTENIMIENTO")) {

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

        disponibilidadRepository
                .findByIdHabitacionAndFecha(idHabitacion, fecha)
                .ifPresent(disponibilidad -> {

                    if (idDisponibilidad == null ||
                            !disponibilidad.getId().equals(idDisponibilidad)) {

                        throw new IllegalArgumentException(
                                "Ya existe disponibilidad para esta habitación en esa fecha"
                        );
                    }
                });
    }
}