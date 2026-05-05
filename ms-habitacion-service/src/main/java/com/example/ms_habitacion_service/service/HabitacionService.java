package com.example.ms_habitacion_service.service;
import com.example.ms_habitacion_service.model.Habitacion;
import com.example.ms_habitacion_service.repository.HabitacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class HabitacionService {

    private final HabitacionRepository habitacionRepository;

    public List<Habitacion> findAll() {
        return habitacionRepository.findAll();
    }

    public Habitacion findById(Long id) {
        return habitacionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Habitación no encontrada con id: " + id));
    }

    public Habitacion guardar(Habitacion habitacion) {
        return habitacionRepository.save(habitacion);
    }

    public Habitacion actualizar(Long id, Habitacion habitacionActualizada) {
        Habitacion habitacionExistente = findById(id);

        habitacionExistente.setNumeroHabitacion(habitacionActualizada.getNumeroHabitacion());
        habitacionExistente.setTipoHabitacion(habitacionActualizada.getTipoHabitacion());
        habitacionExistente.setPrecioBase(habitacionActualizada.getPrecioBase());
        habitacionExistente.setCapacidad(habitacionActualizada.getCapacidad());
        habitacionExistente.setEstadoHabitacion(habitacionActualizada.getEstadoHabitacion());

        return habitacionRepository.save(habitacionExistente);
    }

    public void eliminar(Long id) {
        Habitacion habitacion = findById(id);
        habitacionRepository.delete(habitacion);
    }
}