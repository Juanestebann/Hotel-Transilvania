package com.example.ms_resena_service.service;

import com.example.ms_resena_service.client.ClienteClient;
import com.example.ms_resena_service.client.HabitacionClient;
import com.example.ms_resena_service.client.HotelClient;
import com.example.ms_resena_service.model.Resena;
import com.example.ms_resena_service.repository.ResenaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResenaService {

    private final ResenaRepository resenaRepository;
    private final ClienteClient clienteClient;
    private final HotelClient hotelClient;
    private final HabitacionClient habitacionClient;

    public List<Resena> listarResenas() {
        log.info("Listando todas las reseñas");
        return resenaRepository.findAll();
    }

    public Resena buscarPorId(Long id) {
        log.info("Buscando reseña con id: {}", id);

        return resenaRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("No se encontró la reseña con id: {}", id);
                    return new NoSuchElementException("No se encontró la reseña con ID: " + id);
                });
    }

    public Resena guardarResena(Resena resena) {
        log.info("Creando reseña para cliente id: {}, hotel id: {}, habitación id: {}",
                resena.getIdCliente(), resena.getIdHotel(), resena.getIdHabitacion());

        log.info("Validando cliente id: {}", resena.getIdCliente());
        clienteClient.obtenerClientePorId(resena.getIdCliente());

        log.info("Validando hotel id: {}", resena.getIdHotel());
        hotelClient.obtenerHotelPorId(resena.getIdHotel());

        log.info("Validando habitación id: {}", resena.getIdHabitacion());
        habitacionClient.obtenerHabitacionPorId(resena.getIdHabitacion());

        Resena resenaGuardada = resenaRepository.save(resena);

        log.info("Reseña creada correctamente con id: {}", resenaGuardada.getId());

        return resenaGuardada;
    }

    public Resena actualizarResena(Long id, Resena resenaActualizada) {
        log.info("Actualizando reseña con id: {}", id);

        Resena resena = buscarPorId(id);

        log.info("Validando cliente id: {}", resenaActualizada.getIdCliente());
        clienteClient.obtenerClientePorId(resenaActualizada.getIdCliente());

        log.info("Validando hotel id: {}", resenaActualizada.getIdHotel());
        hotelClient.obtenerHotelPorId(resenaActualizada.getIdHotel());

        log.info("Validando habitación id: {}", resenaActualizada.getIdHabitacion());
        habitacionClient.obtenerHabitacionPorId(resenaActualizada.getIdHabitacion());

        resena.setIdCliente(resenaActualizada.getIdCliente());
        resena.setIdHotel(resenaActualizada.getIdHotel());
        resena.setIdHabitacion(resenaActualizada.getIdHabitacion());
        resena.setIdReserva(resenaActualizada.getIdReserva());
        resena.setCalificacion(resenaActualizada.getCalificacion());
        resena.setComentario(resenaActualizada.getComentario());
        resena.setEstadoResena(resenaActualizada.getEstadoResena());

        Resena resenaGuardada = resenaRepository.save(resena);

        log.info("Reseña actualizada correctamente con id: {}", id);

        return resenaGuardada;
    }

    public void eliminarResena(Long id) {
        log.warn("Solicitando eliminación de reseña con id: {}", id);

        Resena resena = buscarPorId(id);
        resenaRepository.delete(resena);

        log.info("Reseña eliminada correctamente con id: {}", id);
    }

    public List<Resena> buscarPorCliente(Long idCliente) {
        log.info("Buscando reseñas por cliente id: {}", idCliente);
        return resenaRepository.findByIdCliente(idCliente);
    }

    public List<Resena> buscarPorHotel(Long idHotel) {
        log.info("Buscando reseñas por hotel id: {}", idHotel);
        return resenaRepository.findByIdHotel(idHotel);
    }

    public List<Resena> buscarPorHabitacion(Long idHabitacion) {
        log.info("Buscando reseñas por habitación id: {}", idHabitacion);
        return resenaRepository.findByIdHabitacion(idHabitacion);
    }

    public List<Resena> buscarPorReserva(Long idReserva) {
        log.info("Buscando reseñas por reserva id: {}", idReserva);
        return resenaRepository.findByIdReserva(idReserva);
    }

    public List<Resena> buscarPorCalificacion(Integer calificacion) {
        log.info("Buscando reseñas por calificación: {}", calificacion);
        return resenaRepository.findByCalificacion(calificacion);
    }

    public List<Resena> buscarPorEstado(String estadoResena) {
        log.info("Buscando reseñas por estado: {}", estadoResena);
        return resenaRepository.findByEstadoResena(estadoResena);
    }
}