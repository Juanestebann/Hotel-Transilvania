package com.example.ms_reserva_service.service;

import com.example.ms_reserva_service.client.ClienteClient;
import com.example.ms_reserva_service.client.DisponibilidadClient;
import com.example.ms_reserva_service.client.HabitacionClient;
import com.example.ms_reserva_service.client.HotelClient;
import com.example.ms_reserva_service.client.UsuarioClient;
import com.example.ms_reserva_service.dto.DisponibilidadDTO;
import com.example.ms_reserva_service.dto.HabitacionDTO;
import com.example.ms_reserva_service.dto.UsuarioDTO;
import com.example.ms_reserva_service.model.Reserva;
import com.example.ms_reserva_service.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

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

        UsuarioDTO usuarioActual = usuarioClient.obtenerUsuarioActual();
        Reserva reserva = buscarPorId(id);

        validarAccesoAReserva(usuarioActual, reserva);

        return reserva;
    }

    private Reserva buscarPorId(Long id) {
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

        UsuarioDTO usuarioActual = usuarioClient.obtenerUsuarioActual();
        boolean validarUsuarioDestino = prepararCreacionSegunRol(reserva, usuarioActual);

        validarReservaCompleta(reserva, validarUsuarioDestino);

        reserva.setEstadoReserva(reserva.getEstadoReserva().toUpperCase());
        reserva.setFechaCreacion(LocalDateTime.now());

        List<DisponibilidadDTO> disponibilidadesOcupadas = List.of();
        if (debeOcuparDisponibilidad(reserva)) {
            disponibilidadesOcupadas = marcarDisponibilidadComoOcupada(reserva);
        }

        Reserva reservaGuardada;
        try {
            reservaGuardada = reservaRepository.save(reserva);
        } catch (RuntimeException exception) {
            throw resolverFalloConCompensacion(
                    disponibilidadesOcupadas,
                    "DISPONIBLE",
                    exception,
                    "crear la reserva confirmada"
            );
        }

        log.info("Reserva creada correctamente con id: {}", reservaGuardada.getId());

        return reservaGuardada;
    }

    public Reserva actualizar(Long id, Reserva reservaActualizada) {
        log.info("Actualizando reserva con id: {}", id);

        Reserva reservaExistente = findById(id);
        String estadoAnterior = reservaExistente.getEstadoReserva().toUpperCase();
        String nuevoEstado = reservaActualizada.getEstadoReserva().toUpperCase();
        validarTransicionEstado(estadoAnterior, nuevoEstado);

        List<DisponibilidadDTO> disponibilidadesLiberadas = List.of();
        if (debeOcuparDisponibilidad(reservaExistente)) {
            log.info("Liberando disponibilidad anterior para reserva id: {}", id);
            disponibilidadesLiberadas = liberarDisponibilidadReserva(reservaExistente);
        }

        List<DisponibilidadDTO> disponibilidadesOcupadas = List.of();
        try {
            validarReservaCompleta(reservaActualizada, true);
            if (debeOcuparDisponibilidad(reservaActualizada)) {
                disponibilidadesOcupadas = marcarDisponibilidadComoOcupada(reservaActualizada);
            }
        } catch (RuntimeException exception) {
            throw resolverFalloConCompensacion(
                    disponibilidadesLiberadas,
                    "OCUPADA",
                    exception,
                    "restaurar la reserva anterior"
            );
        }

        reservaExistente.setIdCliente(reservaActualizada.getIdCliente());
        reservaExistente.setIdUsuario(reservaActualizada.getIdUsuario());
        reservaExistente.setIdHotel(reservaActualizada.getIdHotel());
        reservaExistente.setIdHabitacion(reservaActualizada.getIdHabitacion());
        reservaExistente.setFechaInicio(reservaActualizada.getFechaInicio());
        reservaExistente.setFechaFin(reservaActualizada.getFechaFin());
        reservaExistente.setCantidadPersonas(reservaActualizada.getCantidadPersonas());
        reservaExistente.setEstadoReserva(reservaActualizada.getEstadoReserva().toUpperCase());

        Reserva reservaGuardada;
        try {
            reservaGuardada = reservaRepository.save(reservaExistente);
        } catch (RuntimeException exception) {
            RuntimeException fallo = resolverFalloConCompensacion(
                    disponibilidadesOcupadas,
                    "DISPONIBLE",
                    exception,
                    "deshacer la nueva disponibilidad"
            );
            throw resolverFalloConCompensacion(
                    disponibilidadesLiberadas,
                    "OCUPADA",
                    fallo,
                    "restaurar la reserva anterior"
            );
        }

        log.info("Reserva actualizada correctamente con id: {}", id);

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

    private void validarReservaCompleta(Reserva reserva, boolean validarUsuarioDestino) {
        log.info("Validando reserva completa");

        validarEstadoReserva(reserva);

        validarFechas(reserva);

        log.info("Validando cliente id: {}", reserva.getIdCliente());
        clienteClient.obtenerClientePorId(reserva.getIdCliente());

        if (validarUsuarioDestino) {
            log.info("Validando usuario destino id: {}", reserva.getIdUsuario());
            usuarioClient.obtenerUsuarioPorId(reserva.getIdUsuario());
        } else {
            log.info("Usuario de la reserva validado mediante /usuarios/me");
        }

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

    private List<DisponibilidadDTO> marcarDisponibilidadComoOcupada(Reserva reserva) {
        log.info("Marcando disponibilidad como OCUPADA para habitación id: {} entre {} y {}",
                reserva.getIdHabitacion(),
                reserva.getFechaInicio(),
                reserva.getFechaFin());

        LocalDate fechaActual = reserva.getFechaInicio();
        List<DisponibilidadDTO> disponibilidadesActualizadas = new ArrayList<>();

        while (fechaActual.isBefore(reserva.getFechaFin())) {
            log.info("Actualizando disponibilidad a OCUPADA. Habitación id: {}, fecha: {}",
                    reserva.getIdHabitacion(),
                    fechaActual);

            DisponibilidadDTO disponibilidad = null;
            try {
                disponibilidad = disponibilidadClient.obtenerDisponibilidadPorHabitacionYFecha(
                        reserva.getIdHabitacion(),
                        fechaActual
                );

                disponibilidad.setEstado("OCUPADA");

                disponibilidadClient.actualizarDisponibilidad(
                        disponibilidad.getId(),
                        disponibilidad
                );
                disponibilidadesActualizadas.add(disponibilidad);
            } catch (RuntimeException exception) {
                if (disponibilidad != null && esFalloRemotoAmbiguo(exception)) {
                    disponibilidadesActualizadas.add(disponibilidad);
                }
                throw resolverFalloConCompensacion(
                        disponibilidadesActualizadas,
                        "DISPONIBLE",
                        exception,
                        "ocupar las disponibilidades"
                );
            }

            fechaActual = fechaActual.plusDays(1);
        }

        return disponibilidadesActualizadas;
    }

    private List<DisponibilidadDTO> liberarDisponibilidadReserva(Reserva reserva) {
        log.info("Liberando disponibilidad para habitación id: {} entre {} y {}",
                reserva.getIdHabitacion(),
                reserva.getFechaInicio(),
                reserva.getFechaFin());

        LocalDate fechaActual = reserva.getFechaInicio();
        List<DisponibilidadDTO> disponibilidadesActualizadas = new ArrayList<>();

        while (fechaActual.isBefore(reserva.getFechaFin())) {
            log.info("Actualizando disponibilidad a DISPONIBLE. Habitación id: {}, fecha: {}",
                    reserva.getIdHabitacion(),
                    fechaActual);

            DisponibilidadDTO disponibilidad = null;
            try {
                disponibilidad = disponibilidadClient.obtenerDisponibilidadPorHabitacionYFecha(
                        reserva.getIdHabitacion(),
                        fechaActual
                );

                disponibilidad.setEstado("DISPONIBLE");

                disponibilidadClient.actualizarDisponibilidad(
                        disponibilidad.getId(),
                        disponibilidad
                );
                disponibilidadesActualizadas.add(disponibilidad);
            } catch (RuntimeException exception) {
                if (disponibilidad != null && esFalloRemotoAmbiguo(exception)) {
                    disponibilidadesActualizadas.add(disponibilidad);
                }
                throw resolverFalloConCompensacion(
                        disponibilidadesActualizadas,
                        "OCUPADA",
                        exception,
                        "liberar las disponibilidades"
                );
            }

            fechaActual = fechaActual.plusDays(1);
        }

        return disponibilidadesActualizadas;
    }

    public Reserva cambiarEstado(Long id, String estadoReserva) {
        log.info("Cambiando estado de reserva id: {} a {}", id, estadoReserva);

        UsuarioDTO usuarioActual = usuarioClient.obtenerUsuarioActual();
        Reserva reserva = buscarPorId(id);

        validarAccesoAReserva(usuarioActual, reserva);

        if (esRol(usuarioActual, "USER") && !"CANCELADA".equalsIgnoreCase(estadoReserva)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Un USER solo puede cancelar una reserva propia"
            );
        }

        return aplicarCambioEstado(reserva, estadoReserva);
    }

    public Reserva cambiarEstadoInterno(Long id, String estadoReserva) {
        if (!"CONFIRMADA".equalsIgnoreCase(estadoReserva)
                && !"CANCELADA".equalsIgnoreCase(estadoReserva)) {
            throw new IllegalArgumentException(
                    "El flujo interno de pago solo permite CONFIRMADA o CANCELADA"
            );
        }

        Reserva reserva = buscarPorId(id);
        return aplicarCambioEstado(reserva, estadoReserva);
    }

    private Reserva aplicarCambioEstado(Reserva reserva, String estadoReserva) {
        Long id = reserva.getId();
        String estadoAnterior = reserva.getEstadoReserva().toUpperCase();
        String nuevoEstado = estadoReserva.toUpperCase();

        validarTransicionEstado(estadoAnterior, nuevoEstado);

        if (estadoAnterior.equals(nuevoEstado)) {
            log.info("Reserva id: {} ya se encuentra en estado {}", id, nuevoEstado);
            return reserva;
        }

        if (estadoAnterior.equalsIgnoreCase("CONFIRMADA")
                && !nuevoEstado.equalsIgnoreCase("CONFIRMADA")) {
            log.info("Reserva id: {} deja de estar CONFIRMADA. Liberando disponibilidad", id);
            List<DisponibilidadDTO> disponibilidadesLiberadas = liberarDisponibilidadReserva(reserva);
            reserva.setEstadoReserva(nuevoEstado);

            try {
                return reservaRepository.save(reserva);
            } catch (RuntimeException exception) {
                reserva.setEstadoReserva(estadoAnterior);
                throw resolverFalloConCompensacion(
                        disponibilidadesLiberadas,
                        "OCUPADA",
                        exception,
                        "guardar la cancelación o finalización"
                );
            }
        }

        if (nuevoEstado.equalsIgnoreCase("CONFIRMADA")) {
            log.info("Reserva id: {} pasa a CONFIRMADA. Validando y ocupando disponibilidad", id);
            validarDisponibilidadReserva(reserva);
            List<DisponibilidadDTO> disponibilidadesOcupadas = marcarDisponibilidadComoOcupada(reserva);
            reserva.setEstadoReserva(nuevoEstado);

            try {
                return reservaRepository.save(reserva);
            } catch (RuntimeException exception) {
                reserva.setEstadoReserva(estadoAnterior);
                throw resolverFalloConCompensacion(
                        disponibilidadesOcupadas,
                        "DISPONIBLE",
                        exception,
                        "guardar la confirmación"
                );
            }
        }

        reserva.setEstadoReserva(nuevoEstado);
        Reserva reservaGuardada = reservaRepository.save(reserva);
        log.info("Estado actualizado correctamente para reserva id: {}", id);

        return reservaGuardada;
    }

    private void validarTransicionEstado(String estadoAnterior, String nuevoEstado) {
        if (estadoAnterior.equals(nuevoEstado)) {
            return;
        }

        boolean transicionValida = switch (estadoAnterior) {
            case "PENDIENTE" -> nuevoEstado.equals("CONFIRMADA")
                    || nuevoEstado.equals("CANCELADA");
            case "CONFIRMADA" -> nuevoEstado.equals("CANCELADA")
                    || nuevoEstado.equals("FINALIZADA");
            case "CANCELADA", "FINALIZADA" -> false;
            default -> false;
        };

        if (!transicionValida) {
            throw new IllegalArgumentException(
                    "Transición de reserva inválida: " + estadoAnterior + " -> " + nuevoEstado
            );
        }
    }

    private RuntimeException resolverFalloConCompensacion(
            List<DisponibilidadDTO> disponibilidades,
            String estadoCompensacion,
            RuntimeException falloOriginal,
            String operacion
    ) {
        RuntimeException falloCompensacion = compensarDisponibilidades(
                disponibilidades,
                estadoCompensacion
        );

        if (falloCompensacion == null) {
            return falloOriginal;
        }

        HttpStatus estado = esGatewayTimeout(falloCompensacion)
                ? HttpStatus.GATEWAY_TIMEOUT
                : HttpStatus.SERVICE_UNAVAILABLE;

        return new ResponseStatusException(
                estado,
                "Falló la compensación después de " + operacion,
                falloCompensacion
        );
    }

    private RuntimeException compensarDisponibilidades(
            List<DisponibilidadDTO> disponibilidades,
            String estadoCompensacion
    ) {
        RuntimeException primerFallo = null;

        for (int indice = disponibilidades.size() - 1; indice >= 0; indice--) {
            DisponibilidadDTO disponibilidad = disponibilidades.get(indice);
            disponibilidad.setEstado(estadoCompensacion);

            try {
                disponibilidadClient.actualizarDisponibilidad(
                        disponibilidad.getId(),
                        disponibilidad
                );
            } catch (RuntimeException exception) {
                if (primerFallo == null) {
                    primerFallo = exception;
                }
            }
        }

        return primerFallo;
    }

    private boolean esFalloRemotoAmbiguo(RuntimeException exception) {
        if (!(exception instanceof ResponseStatusException responseStatusException)) {
            return false;
        }

        int estado = responseStatusException.getStatusCode().value();
        return estado == HttpStatus.SERVICE_UNAVAILABLE.value()
                || estado == HttpStatus.GATEWAY_TIMEOUT.value();
    }

    private boolean esGatewayTimeout(RuntimeException exception) {
        return exception instanceof ResponseStatusException responseStatusException
                && responseStatusException.getStatusCode().value()
                == HttpStatus.GATEWAY_TIMEOUT.value();
    }

    private boolean prepararCreacionSegunRol(Reserva reserva, UsuarioDTO usuarioActual) {
        if (esRol(usuarioActual, "ADMIN")) {
            return true;
        }

        if (!esRol(usuarioActual, "USER")) {
            throw accesoDenegado("Rol de usuario no autorizado para crear reservas");
        }

        if (!Objects.equals(usuarioActual.getIdUsuario(), reserva.getIdUsuario())) {
            throw accesoDenegado("No puedes crear una reserva para otro usuario");
        }

        if (!"PENDIENTE".equalsIgnoreCase(reserva.getEstadoReserva())) {
            throw accesoDenegado("Las reservas creadas por USER deben iniciar en estado PENDIENTE");
        }

        reserva.setIdUsuario(usuarioActual.getIdUsuario());
        reserva.setEstadoReserva("PENDIENTE");
        return false;
    }

    private void validarAccesoAReserva(UsuarioDTO usuarioActual, Reserva reserva) {
        if (esRol(usuarioActual, "ADMIN")) {
            return;
        }

        if (!esRol(usuarioActual, "USER")
                || !Objects.equals(usuarioActual.getIdUsuario(), reserva.getIdUsuario())) {
            throw accesoDenegado("No puedes acceder a una reserva de otro usuario");
        }
    }

    private boolean esRol(UsuarioDTO usuario, String rolEsperado) {
        if (usuario == null || usuario.getRol() == null) {
            return false;
        }

        String rol = usuario.getRol().toUpperCase();
        return rolEsperado.equals(rol) || ("ROLE_" + rolEsperado).equals(rol);
    }

    private ResponseStatusException accesoDenegado(String mensaje) {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, mensaje);
    }
}
