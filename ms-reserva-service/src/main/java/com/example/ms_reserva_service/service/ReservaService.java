package com.example.ms_reserva_service.service;

import com.example.ms_reserva_service.client.ClienteClient;
import com.example.ms_reserva_service.client.HabitacionClient;
import com.example.ms_reserva_service.client.HotelClient;
import com.example.ms_reserva_service.client.UsuarioClient;
import com.example.ms_reserva_service.dto.HabitacionDTO;
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
    private final HabitacionClient habitacionClient;
    private final HotelClient hotelClient;

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

        hotelClient.obtenerHotelPorId(reserva.getIdHotel());

        HabitacionDTO habitacion = habitacionClient.obtenerHabitacionPorId(reserva.getIdHabitacion());

        validarHabitacionPerteneceAlHotel(reserva, habitacion);

        validarEstadoHabitacion(habitacion);

        validarCapacidadHabitacion(reserva, habitacion);

        reserva.setFechaCreacion(LocalDateTime.now());

        return reservaRepository.save(reserva);
    }

    public Reserva actualizar(Long id, Reserva reservaActualizada) {

        validarFechas(reservaActualizada);

        clienteClient.obtenerClientePorId(reservaActualizada.getIdCliente());

        usuarioClient.obtenerUsuarioPorId(reservaActualizada.getIdUsuario());

        hotelClient.obtenerHotelPorId(reservaActualizada.getIdHotel());

        HabitacionDTO habitacion = habitacionClient.obtenerHabitacionPorId(reservaActualizada.getIdHabitacion());

        validarHabitacionPerteneceAlHotel(reservaActualizada, habitacion);

        validarEstadoHabitacion(habitacion);

        validarCapacidadHabitacion(reservaActualizada, habitacion);

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

    private void validarHabitacionPerteneceAlHotel(Reserva reserva, HabitacionDTO habitacion) {

        if (!habitacion.getIdHotel().equals(reserva.getIdHotel())) {
            throw new IllegalArgumentException(
                    "La habitación no pertenece al hotel indicado"
            );
        }
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
    private void validarEstadoHabitacion(HabitacionDTO habitacion) {

        if (!habitacion.getEstadoHabitacion().equalsIgnoreCase("DISPONIBLE")) {

            throw new IllegalArgumentException(
                    "La habitación no está disponible para reservar"
            );
        }
    }
    private void validarCapacidadHabitacion(
            Reserva reserva,
            HabitacionDTO habitacion
    ) {

        if (reserva.getCantidadPersonas() > habitacion.getCapacidad()) {

            throw new IllegalArgumentException(
                    "La cantidad de personas excede la capacidad de la habitación"
            );
        }
    }


}