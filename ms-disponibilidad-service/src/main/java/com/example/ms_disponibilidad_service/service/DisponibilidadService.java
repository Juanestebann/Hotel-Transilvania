package com.example.ms_disponibilidad_service.service;
import com.example.ms_disponibilidad_service.model.Disponibilidad;
import com.example.ms_disponibilidad_service.repository.DisponibilidadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class DisponibilidadService {

    private final DisponibilidadRepository disponibilidadRepository;

    public List<Disponibilidad> findAll() {
        return disponibilidadRepository.findAll();
    }

    public Disponibilidad findById(Long id) {
        return disponibilidadRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Disponibilidad no encontrada con id: " + id));
    }

    public Disponibilidad guardar(Disponibilidad disponibilidad) {
        return disponibilidadRepository.save(disponibilidad);
    }

    public Disponibilidad actualizar(Long id, Disponibilidad disponibilidadActualizada) {
        Disponibilidad existente = findById(id);
        existente.setFecha(disponibilidadActualizada.getFecha());
        existente.setEstado(disponibilidadActualizada.getEstado());
        return disponibilidadRepository.save(existente);
    }

    public void eliminar(Long id) {
        Disponibilidad disponibilidad = findById(id);
        disponibilidadRepository.delete(disponibilidad);
    }
}
