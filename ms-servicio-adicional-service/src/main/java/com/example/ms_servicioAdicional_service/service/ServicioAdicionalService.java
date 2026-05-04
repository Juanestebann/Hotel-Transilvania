package com.example.ms_servicioAdicional_service.service;

import com.example.ms_servicioAdicional_service.model.ServicioAdicionalModel;
import com.example.ms_servicioAdicional_service.repository.ServicioAdicionalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ServicioAdicionalService {

    private final ServicioAdicionalRepository servicioAdicionalRepository;

    public List<ServicioAdicionalModel> findAll() {
        return servicioAdicionalRepository.findAll();
    }

    public ServicioAdicionalModel findById(Long id) {
        return servicioAdicionalRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Servicio adicional no encontrado con id: " + id));
    }

    public List<ServicioAdicionalModel> findByIdHotel(Long idHotel) {
        return servicioAdicionalRepository.findByIdHotel(idHotel);
    }

    public List<ServicioAdicionalModel> findByEstado(String estado) {
        return servicioAdicionalRepository.findByEstado(estado);
    }

    public List<ServicioAdicionalModel> findByNombre(String nombre) {
        return servicioAdicionalRepository.findByNombre(nombre);
    }

    public ServicioAdicionalModel guardar(ServicioAdicionalModel servicioAdicional) {
        return servicioAdicionalRepository.save(servicioAdicional);
    }

    public ServicioAdicionalModel actualizar(Long id, ServicioAdicionalModel servicioActualizado) {
        ServicioAdicionalModel servicioExistente = findById(id);

        servicioExistente.setIdHotel(servicioActualizado.getIdHotel());
        servicioExistente.setNombre(servicioActualizado.getNombre());
        servicioExistente.setDescripcion(servicioActualizado.getDescripcion());
        servicioExistente.setPrecio(servicioActualizado.getPrecio());
        servicioExistente.setEstado(servicioActualizado.getEstado());

        return servicioAdicionalRepository.save(servicioExistente);
    }

    public void eliminar(Long id) {
        ServicioAdicionalModel servicio = findById(id);
        servicioAdicionalRepository.delete(servicio);
    }
}