package com.example.ms_resena_service.service;

import com.example.ms_resena_service.client.ReservaClient;
import com.example.ms_resena_service.dto.ReservaDTO;
import com.example.ms_resena_service.model.Resena;
import com.example.ms_resena_service.repository.ResenaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResenaService {

    private final ResenaRepository resenaRepository;
    private final ReservaClient reservaClient;

    public List<Resena> listarResenas() {
        log.info("Listando todas las reseñas");
        return resenaRepository.findAll();
    }

    public Resena buscarPorId(Long id) {
        log.info("Buscando reseña con id: {}", id);

        Resena resena = buscarPorIdSinAutorizar(id);
        reservaClient.obtenerReservaPorId(resena.getIdReserva());
        return resena;
    }

    private Resena buscarPorIdSinAutorizar(Long id) {
        return resenaRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("No se encontró la reseña con id: {}", id);
                    return new NoSuchElementException("No se encontró la reseña con ID: " + id);
                });
    }

    public Resena guardarResena(Resena resena) {
        log.info("Creando reseña para cliente id: {}, hotel id: {}, habitación id: {}",
                resena.getIdCliente(), resena.getIdHotel(), resena.getIdHabitacion());

        ReservaDTO reserva = reservaClient.obtenerReservaPorId(resena.getIdReserva());
        validarYDerivarDatosReserva(resena, reserva);

        Resena resenaGuardada = resenaRepository.save(resena);

        log.info("Reseña creada correctamente con id: {}", resenaGuardada.getId());

        return resenaGuardada;
    }

    public Resena actualizarResena(Long id, Resena resenaActualizada) {
        log.info("Actualizando reseña con id: {}", id);

        Resena resena = buscarPorIdSinAutorizar(id);

        reservaClient.obtenerReservaPorId(resena.getIdReserva());
        ReservaDTO reservaDestino = reservaClient.obtenerReservaPorId(resenaActualizada.getIdReserva());
        validarYDerivarDatosReserva(resenaActualizada, reservaDestino);

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

        Resena resena = buscarPorIdSinAutorizar(id);
        reservaClient.obtenerReservaPorId(resena.getIdReserva());
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
        reservaClient.obtenerReservaPorId(idReserva);
        return resenaRepository.findByIdReserva(idReserva);
    }

    public List<Resena> buscarPorCalificacion(Integer calificacion) {
        log.info("Buscando reseñas por calificación: {}", calificacion);
        return resenaRepository.findByCalificacion(calificacion);
    }

    public List<Resena> buscarPorEstado(String estadoResena) {
        log.info("Buscando reseñas por estado: {}", estadoResena);
        return resenaRepository.findByEstadoResena(estadoResena.toUpperCase());
    }

    private void validarYDerivarDatosReserva(Resena resena, ReservaDTO reserva) {
        if (reserva == null
                || !Objects.equals(resena.getIdCliente(), reserva.getIdCliente())
                || !Objects.equals(resena.getIdHotel(), reserva.getIdHotel())
                || !Objects.equals(resena.getIdHabitacion(), reserva.getIdHabitacion())) {
            throw new IllegalArgumentException(
                    "Cliente, hotel y habitación deben coincidir con la reserva"
            );
        }

        resena.setIdCliente(reserva.getIdCliente());
        resena.setIdHotel(reserva.getIdHotel());
        resena.setIdHabitacion(reserva.getIdHabitacion());
    }
}
