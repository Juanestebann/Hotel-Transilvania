package com.example.ms_reserva_service.service;

import com.example.ms_reserva_service.client.ClienteClient;
import com.example.ms_reserva_service.client.UsuarioClient;
import com.example.ms_reserva_service.model.Reserva;
import com.example.ms_reserva_service.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final ClienteClient clienteClient;
    private final UsuarioClient usuarioClient;

    public List<Reserva> findAll() {
        return reservaRepository.findAll();
    }

    public Reserva findById(Long id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Reserva no encontrada con id: " + id));
    }
    public List<Reserva> findByIdUsuario(Long idUsuario) {
        return reservaRepository.findByIdUsuario(idUsuario);
    }

    public List<Reserva> findByIdCliente(Long idCliente) {
        return reservaRepository.findByIdCliente(idCliente);
    }

    public List<Reserva> findByIdHotel(Long idHotel) {
        return reservaRepository.findByIdHotel(idHotel);
    }

    public List<Reserva> findByIdHabitacion(Long idHabitacion) {
        return reservaRepository.findByIdHabitacion(idHabitacion);
    }

    public List<Reserva> findByEstadoReserva(String estadoReserva) {
        return reservaRepository.findByEstadoReserva(estadoReserva);
    }

    public Reserva guardar(Reserva reserva) {

        validarFechas(reserva);

        clienteClient.obtenerClientePorId(reserva.getIdCliente());

        usuarioClient.obtenerUsuarioPorId(reserva.getIdUsuario());

        reserva.setFechaCreacion(LocalDateTime.now());

        return reservaRepository.save(reserva);
    }

    public Reserva actualizar(Long id, Reserva reservaActualizada) {

        validarFechas(reservaActualizada);

        clienteClient.obtenerClientePorId(reservaActualizada.getIdCliente());

        usuarioClient.obtenerUsuarioPorId(reservaActualizada.getIdUsuario());

        Reserva reservaExistente = findById(id);

        reservaExistente.setIdCliente(reservaActualizada.getIdCliente());

        reservaExistente.setIdUsuario(reservaActualizada.getIdUsuario());

        reservaExistente.setIdHotel(reservaActualizada.getIdHotel());

        reservaExistente.setIdHabitacion(reservaActualizada.getIdHabitacion());

        reservaExistente.setFechaInicio(reservaActualizada.getFechaInicio());

        reservaExistente.setFechaFin(reservaActualizada.getFechaFin());

        reservaExistente.setCantidadPersonas(reservaActualizada.getCantidadPersonas());

        reservaExistente.setEstadoReserva(reservaActualizada.getEstadoReserva());

        return reservaRepository.save(reservaExistente);
    }

    public void eliminar(Long id) {
        Reserva reserva = findById(id);
        reservaRepository.delete(reserva);
    }

    private void validarFechas(Reserva reserva) {
        if (reserva.getFechaInicio() != null && reserva.getFechaFin() != null) {
            if (!reserva.getFechaFin().isAfter(reserva.getFechaInicio())) {
                throw new IllegalArgumentException("La fechaFin debe ser posterior a la fechaInicio");
            }
        }
    }
}