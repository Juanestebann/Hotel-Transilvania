package com.example.ms_resena_service.service;

import com.example.ms_resena_service.client.ClienteClient;
import com.example.ms_resena_service.client.HabitacionClient;
import com.example.ms_resena_service.client.HotelClient;
import com.example.ms_resena_service.model.Resena;
import com.example.ms_resena_service.repository.ResenaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ResenaService {

    private final ResenaRepository resenaRepository;
    private final ClienteClient clienteClient;
    private final HotelClient hotelClient;
    private final HabitacionClient habitacionClient;

    public List<Resena> listarResenas() {
        return resenaRepository.findAll();
    }

    public Resena buscarPorId(Long id) {
        return resenaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No se encontró la reseña con ID: " + id));
    }

    public Resena guardarResena(Resena resena) {

        // Validar cliente existente
        clienteClient.obtenerClientePorId(resena.getIdCliente());

        // Validar hotel existente
        hotelClient.obtenerHotelPorId(resena.getIdHotel());

        // Validar habitación existente
        habitacionClient.obtenerHabitacionPorId(resena.getIdHabitacion());

        return resenaRepository.save(resena);
    }

    public Resena actualizarResena(Long id, Resena resenaActualizada) {
        Resena resena = buscarPorId(id);

        // Validar cliente existente
        clienteClient.obtenerClientePorId(resenaActualizada.getIdCliente());

        // Validar hotel existente
        hotelClient.obtenerHotelPorId(resenaActualizada.getIdHotel());

        // Validar habitación existente
        habitacionClient.obtenerHabitacionPorId(resenaActualizada.getIdHabitacion());

        resena.setIdCliente(resenaActualizada.getIdCliente());
        resena.setIdHotel(resenaActualizada.getIdHotel());
        resena.setIdHabitacion(resenaActualizada.getIdHabitacion());
        resena.setIdReserva(resenaActualizada.getIdReserva());
        resena.setCalificacion(resenaActualizada.getCalificacion());
        resena.setComentario(resenaActualizada.getComentario());
        resena.setEstadoResena(resenaActualizada.getEstadoResena());

        return resenaRepository.save(resena);
    }

    public void eliminarResena(Long id) {
        Resena resena = buscarPorId(id);
        resenaRepository.delete(resena);
    }

    public List<Resena> buscarPorCliente(Long idCliente) {
        return resenaRepository.findByIdCliente(idCliente);
    }

    public List<Resena> buscarPorHotel(Long idHotel) {
        return resenaRepository.findByIdHotel(idHotel);
    }

    public List<Resena> buscarPorHabitacion(Long idHabitacion) {
        return resenaRepository.findByIdHabitacion(idHabitacion);
    }

    public List<Resena> buscarPorReserva(Long idReserva) {
        return resenaRepository.findByIdReserva(idReserva);
    }

    public List<Resena> buscarPorCalificacion(Integer calificacion) {
        return resenaRepository.findByCalificacion(calificacion);
    }

    public List<Resena> buscarPorEstado(String estadoResena) {
        return resenaRepository.findByEstadoResena(estadoResena);
    }
}