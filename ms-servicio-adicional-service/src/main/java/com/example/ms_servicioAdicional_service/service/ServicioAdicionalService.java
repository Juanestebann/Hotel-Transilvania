package com.example.ms_servicioAdicional_service.service;

import com.example.ms_servicioAdicional_service.client.HotelClient;
import com.example.ms_servicioAdicional_service.client.ReservaClient;
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
    private final HotelClient hotelClient;
    private final ReservaClient reservaClient;

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

    public List<ServicioAdicionalModel> findByIdReserva(Long idReserva) {
        return servicioAdicionalRepository.findByIdReserva(idReserva);
    }

    public List<ServicioAdicionalModel> findByEstado(String estado) {
        return servicioAdicionalRepository.findByEstado(estado);
    }

    public List<ServicioAdicionalModel> findByNombre(String nombre) {
        return servicioAdicionalRepository.findByNombre(nombre);
    }

    public ServicioAdicionalModel guardar(ServicioAdicionalModel servicioAdicional) {

        // Validar hotel existente
        hotelClient.obtenerHotelPorId(servicioAdicional.getIdHotel());

        // Validar reserva existente si viene idReserva
        if (servicioAdicional.getIdReserva() != null) {
            reservaClient.obtenerReservaPorId(servicioAdicional.getIdReserva());
        }

        return servicioAdicionalRepository.save(servicioAdicional);
    }

    public ServicioAdicionalModel actualizar(Long id, ServicioAdicionalModel servicioActualizado) {
        ServicioAdicionalModel servicioExistente = findById(id);

        // Validar hotel existente
        hotelClient.obtenerHotelPorId(servicioActualizado.getIdHotel());

        // Validar reserva existente si viene idReserva
        if (servicioActualizado.getIdReserva() != null) {
            reservaClient.obtenerReservaPorId(servicioActualizado.getIdReserva());
        }

        servicioExistente.setIdHotel(servicioActualizado.getIdHotel());
        servicioExistente.setIdReserva(servicioActualizado.getIdReserva());
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