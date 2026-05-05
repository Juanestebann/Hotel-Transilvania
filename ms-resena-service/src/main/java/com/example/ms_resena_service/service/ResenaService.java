package com.example.ms_resena_service.service;

import com.example.ms_resena_service.model.Resena;
import com.example.ms_resena_service.repository.ResenaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ResenaService {

    private final ResenaRepository resenaRepository;

    public ResenaService(ResenaRepository resenaRepository) {
        this.resenaRepository = resenaRepository;
    }

    public List<Resena> listarResenas() {
        return resenaRepository.findAll();
    }

    public Resena buscarPorId(Long id) {
        return resenaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No se encontró la reseña con ID: " + id));
    }

    public Resena guardarResena(Resena resena) {
        return resenaRepository.save(resena);
    }

    public Resena actualizarResena(Long id, Resena resenaActualizada) {
        Resena resena = buscarPorId(id);

        resena.setIdCliente(resenaActualizada.getIdCliente());
        resena.setIdHotel(resenaActualizada.getIdHotel());
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