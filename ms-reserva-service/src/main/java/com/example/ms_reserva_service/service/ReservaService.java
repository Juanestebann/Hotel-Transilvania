package com.example.ms_reserva_service.service;

import com.example.ms_reserva_service.client.ClienteClient;
import com.example.ms_reserva_service.client.DisponibilidadClient;
import com.example.ms_reserva_service.client.HabitacionClient;
import com.example.ms_reserva_service.client.HotelClient;
import com.example.ms_reserva_service.client.UsuarioClient;
import com.example.ms_reserva_service.dto.DisponibilidadDTO;
import com.example.ms_reserva_service.dto.HabitacionDTO;
import com.example.ms_reserva_service.model.Reserva;
import com.example.ms_reserva_service.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final ClienteClient clienteClient;
    private final UsuarioClient usuarioClient;
    private final HabitacionClient habitacionClient;
    private final HotelClient hotelClient;
    private final DisponibilidadClient disponibilidadClient;

    public List<Reserva> findAll() {
        log.info("Listando todas las reservas");
        return reservaRepository.findAll();
    }

    public Reserva findById(Long id) {
        log.info("Buscando reserva con id: {}", id);

        return reservaRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Reserva no encontrada con id: {}", id);
                    return new NoSuchElementException("Reserva no encontrada con id: " + id);
                });
    }

    public List<Reserva> findByIdUsuario(Long idUsuario) {
        log.info("Buscando reservas por usuario id: {}", idUsuario);
        return reservaRepository.findByIdUsuario(idUsuario);
    }

    public List<Reserva> findByIdCliente(Long idCliente) {
        log.info("Buscando reservas por cliente id: {}", idCliente);
        return reservaRepository.findByIdCliente(idCliente);
    }

    public List<Reserva> findByIdHotel(Long idHotel) {
        log.info("Buscando reservas por hotel id: {}", idHotel);
        return reservaRepository.findByIdHotel(idHotel);
    }

    public List<Reserva> findByIdHabitacion(Long idHabitacion) {
        log.info("Buscando reservas por habitación id: {}", idHabitacion);
        return reservaRepository.findByIdHabitacion(idHabitacion);
    }

    public List<Reserva> findByEstadoReserva(String estadoReserva) {
        log.info("Buscando reservas por estado: {}", estadoReserva);
        return reservaRepository.findByEstadoReserva(estadoReserva);
    }

    public Reserva guardar(Reserva reserva) {
        log.info("Creando reserva para cliente id: {}, usuario id: {}, hotel id: {}, habitacion id: {}",
                reserva.getIdCliente(),
                reserva.getIdUsuario(),
                reserva.getIdHotel(),
                reserva.getIdHabitacion());

        validarReservaCompleta(reserva);

        reserva.setEstadoReserva(reserva.getEstadoReserva().toUpperCase());
        reserva.setFechaCreacion(LocalDateTime.now());

        Reserva reservaGuardada = reservaRepository.save(reserva);

        log.info("Reserva creada correctamente con id: {}", reservaGuardada.getId());

        if (debeOcuparDisponibilidad(reservaGuardada)) {
            log.info("Reserva id: {} está CONFIRMADA. Marcando disponibilidad como OCUPADA",
                    reservaGuardada.getId());

            marcarDisponibilidadComoOcupada(reservaGuardada);
        }

        return reservaGuardada;
    }

    public Reserva actualizar(Long id, Reserva reservaActualizada) {
        log.info("Actualizando reserva con id: {}", id);

        Reserva reservaExistente = findById(id);

        if (debeOcuparDisponibilidad(reservaExistente)) {
            log.info("Liberando disponibilidad anterior para reserva id: {}", id);
            liberarDisponibilidadReserva(reservaExistente);
        }

        validarReservaCompleta(reservaActualizada);

        reservaExistente.setIdCliente(reservaActualizada.getIdCliente());
        reservaExistente.setIdUsuario(reservaActualizada.getIdUsuario());
        reservaExistente.setIdHotel(reservaActualizada.getIdHotel());
        reservaExistente.setIdHabitacion(reservaActualizada.getIdHabitacion());
        reservaExistente.setFechaInicio(reservaActualizada.getFechaInicio());
        reservaExistente.setFechaFin(reservaActualizada.getFechaFin());
        reservaExistente.setCantidadPersonas(reservaActualizada.getCantidadPersonas());
        reservaExistente.setEstadoReserva(reservaActualizada.getEstadoReserva().toUpperCase());

        Reserva reservaGuardada = reservaRepository.save(reservaExistente);

        log.info("Reserva actualizada correctamente con id: {}", id);

        if (debeOcuparDisponibilidad(reservaGuardada)) {
            log.info("Reserva id: {} está CONFIRMADA. Marcando nueva disponibilidad como OCUPADA",
                    reservaGuardada.getId());

            marcarDisponibilidadComoOcupada(reservaGuardada);
        }

        return reservaGuardada;
    }

    public void eliminar(Long id) {
        log.warn("Solicitando eliminación de reserva con id: {}", id);

        Reserva reserva = findById(id);

        log.info("Liberando disponibilidad antes de eliminar reserva id: {}", id);
        liberarDisponibilidadReserva(reserva);

        reservaRepository.delete(reserva);

        log.info("Reserva eliminada correctamente con id: {}", id);
    }

    private void validarReservaCompleta(Reserva reserva) {
        log.info("Validando reserva completa");

        validarEstadoReserva(reserva);

        validarFechas(reserva);

        log.info("Validando cliente id: {}", reserva.getIdCliente());
        clienteClient.obtenerClientePorId(reserva.getIdCliente());

        log.info("Validando usuario id: {}", reserva.getIdUsuario());
        usuarioClient.obtenerUsuarioPorId(reserva.getIdUsuario());

        log.info("Validando hotel id: {}", reserva.getIdHotel());
        hotelClient.obtenerHotelPorId(reserva.getIdHotel());

        log.info("Validando habitación id: {}", reserva.getIdHabitacion());
        HabitacionDTO habitacion = habitacionClient.obtenerHabitacionPorId(
                reserva.getIdHabitacion()
        );

        validarHabitacionPerteneceAlHotel(reserva, habitacion);

        validarEstadoHabitacion(habitacion);

        validarCapacidadHabitacion(reserva, habitacion);

        if (reserva.getEstadoReserva().equalsIgnoreCase("CONFIRMADA")) {
            log.info("Reserva CONFIRMADA. Validando disponibilidad por fechas");
            validarDisponibilidadReserva(reserva);
        }
    }

    private boolean debeOcuparDisponibilidad(Reserva reserva) {
        return reserva.getEstadoReserva().equalsIgnoreCase("CONFIRMADA");
    }

    private void validarHabitacionPerteneceAlHotel(Reserva reserva, HabitacionDTO habitacion) {
        log.info("Validando que habitación id: {} pertenezca al hotel id: {}",
                reserva.getIdHabitacion(),
                reserva.getIdHotel());

        if (!habitacion.getIdHotel().equals(reserva.getIdHotel())) {
            log.error("La habitación id: {} no pertenece al hotel id: {}",
                    reserva.getIdHabitacion(),
                    reserva.getIdHotel());

            throw new IllegalArgumentException(
                    "La habitación no pertenece al hotel indicado"
            );
        }
    }

    private void validarFechas(Reserva reserva) {
        log.info("Validando fechas de reserva. Inicio: {}, Fin: {}",
                reserva.getFechaInicio(),
                reserva.getFechaFin());

        if (reserva.getFechaInicio() != null && reserva.getFechaFin() != null) {
            if (!reserva.getFechaFin().isAfter(reserva.getFechaInicio())) {
                log.error("Fechas inválidas. fechaFin: {} no es posterior a fechaInicio: {}",
                        reserva.getFechaFin(),
                        reserva.getFechaInicio());

                throw new IllegalArgumentException("La fechaFin debe ser posterior a la fechaInicio");
            }
        }
    }

    private void validarEstadoReserva(Reserva reserva) {
        log.info("Validando estado de reserva: {}", reserva.getEstadoReserva());

        if (!reserva.getEstadoReserva().equalsIgnoreCase("CONFIRMADA") &&
                !reserva.getEstadoReserva().equalsIgnoreCase("PENDIENTE") &&
                !reserva.getEstadoReserva().equalsIgnoreCase("CANCELADA") &&
                !reserva.getEstadoReserva().equalsIgnoreCase("FINALIZADA")) {

            log.error("EstadoReserva inválido recibido: {}", reserva.getEstadoReserva());

            throw new IllegalArgumentException(
                    "EstadoReserva inválido. Use: CONFIRMADA, PENDIENTE, CANCELADA o FINALIZADA"
            );
        }
    }

    private void validarEstadoHabitacion(HabitacionDTO habitacion) {
        log.info("Validando estado de habitación: {}", habitacion.getEstadoHabitacion());

        if (!habitacion.getEstadoHabitacion().equalsIgnoreCase("DISPONIBLE")) {
            log.error("La habitación no está disponible. Estado actual: {}",
                    habitacion.getEstadoHabitacion());

            throw new IllegalArgumentException(
                    "La habitación no está disponible para reservar"
            );
        }
    }

    private void validarCapacidadHabitacion(Reserva reserva, HabitacionDTO habitacion) {
        log.info("Validando capacidad. Personas: {}, capacidad habitación: {}",
                reserva.getCantidadPersonas(),
                habitacion.getCapacidad());

        if (reserva.getCantidadPersonas() > habitacion.getCapacidad()) {
            log.error("Cantidad de personas {} excede capacidad de habitación {}",
                    reserva.getCantidadPersonas(),
                    habitacion.getCapacidad());

            throw new IllegalArgumentException(
                    "La cantidad de personas excede la capacidad de la habitación"
            );
        }
    }

    private void validarDisponibilidadReserva(Reserva reserva) {
        log.info("Validando disponibilidad para habitación id: {} entre {} y {}",
                reserva.getIdHabitacion(),
                reserva.getFechaInicio(),
                reserva.getFechaFin());

        LocalDate fechaActual = reserva.getFechaInicio();

        while (fechaActual.isBefore(reserva.getFechaFin())) {
            log.info("Consultando disponibilidad para habitación id: {} en fecha: {}",
                    reserva.getIdHabitacion(),
                    fechaActual);

            DisponibilidadDTO disponibilidad =
                    disponibilidadClient.obtenerDisponibilidadPorHabitacionYFecha(
                            reserva.getIdHabitacion(),
                            fechaActual
                    );

            if (!disponibilidad.getEstado().equalsIgnoreCase("DISPONIBLE")) {
                log.error("Habitación id: {} no disponible en fecha: {}. Estado actual: {}",
                        reserva.getIdHabitacion(),
                        fechaActual,
                        disponibilidad.getEstado());

                throw new IllegalArgumentException(
                        "La habitación no está disponible en la fecha " + fechaActual
                );
            }

            fechaActual = fechaActual.plusDays(1);
        }
    }

    private void marcarDisponibilidadComoOcupada(Reserva reserva) {
        log.info("Marcando disponibilidad como OCUPADA para habitación id: {} entre {} y {}",
                reserva.getIdHabitacion(),
                reserva.getFechaInicio(),
                reserva.getFechaFin());

        LocalDate fechaActual = reserva.getFechaInicio();

        while (fechaActual.isBefore(reserva.getFechaFin())) {
            log.info("Actualizando disponibilidad a OCUPADA. Habitación id: {}, fecha: {}",
                    reserva.getIdHabitacion(),
                    fechaActual);

            DisponibilidadDTO disponibilidad =
                    disponibilidadClient.obtenerDisponibilidadPorHabitacionYFecha(
                            reserva.getIdHabitacion(),
                            fechaActual
                    );

            disponibilidad.setEstado("OCUPADA");

            disponibilidadClient.actualizarDisponibilidad(
                    disponibilidad.getId(),
                    disponibilidad
            );

            fechaActual = fechaActual.plusDays(1);
        }
    }

    private void liberarDisponibilidadReserva(Reserva reserva) {
        log.info("Liberando disponibilidad para habitación id: {} entre {} y {}",
                reserva.getIdHabitacion(),
                reserva.getFechaInicio(),
                reserva.getFechaFin());

        LocalDate fechaActual = reserva.getFechaInicio();

        while (fechaActual.isBefore(reserva.getFechaFin())) {
            log.info("Actualizando disponibilidad a DISPONIBLE. Habitación id: {}, fecha: {}",
                    reserva.getIdHabitacion(),
                    fechaActual);

            DisponibilidadDTO disponibilidad =
                    disponibilidadClient.obtenerDisponibilidadPorHabitacionYFecha(
                            reserva.getIdHabitacion(),
                            fechaActual
                    );

            disponibilidad.setEstado("DISPONIBLE");

            disponibilidadClient.actualizarDisponibilidad(
                    disponibilidad.getId(),
                    disponibilidad
            );

            fechaActual = fechaActual.plusDays(1);
        }
    }

    public Reserva cambiarEstado(Long id, String estadoReserva) {
        log.info("Cambiando estado de reserva id: {} a {}", id, estadoReserva);

        Reserva reserva = findById(id);
        String estadoAnterior = reserva.getEstadoReserva();
        String nuevoEstado = estadoReserva.toUpperCase();

        reserva.setEstadoReserva(nuevoEstado);
        validarEstadoReserva(reserva);

        if (estadoAnterior.equalsIgnoreCase("CONFIRMADA")
                && !nuevoEstado.equalsIgnoreCase("CONFIRMADA")) {
            log.info("Reserva id: {} deja de estar CONFIRMADA. Liberando disponibilidad", id);
            liberarDisponibilidadReserva(reserva);
            reserva.setEstadoReserva(nuevoEstado);
        }

        if (debeOcuparDisponibilidad(reserva)
                && !estadoAnterior.equalsIgnoreCase("CONFIRMADA")) {
            log.info("Reserva id: {} pasa a CONFIRMADA. Validando y ocupando disponibilidad", id);
            validarDisponibilidadReserva(reserva);
        }

        Reserva reservaGuardada = reservaRepository.save(reserva);

        if (debeOcuparDisponibilidad(reservaGuardada)
                && !estadoAnterior.equalsIgnoreCase("CONFIRMADA")) {
            marcarDisponibilidadComoOcupada(reservaGuardada);
        }

        log.info("Estado actualizado correctamente para reserva id: {}", id);

        return reservaGuardada;
    }
}