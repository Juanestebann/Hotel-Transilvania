package com.example.ms_habitacion_service.service;
import com.example.ms_habitacion_service.client.HotelClient;
import com.example.ms_habitacion_service.model.Habitacion;
import com.example.ms_habitacion_service.repository.HabitacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class HabitacionService {

    private final HabitacionRepository habitacionRepository;
    private final HotelClient hotelClient;

    public List<Habitacion> findAll() {
        log.info("Listando todas las habitaciones");
        return habitacionRepository.findAll();
    }

    public Habitacion findById(Long id) {
        log.info("Buscando habitación con id: {}", id);
        return habitacionRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Habitación no encontrada con id: {}", id);
                    return new NoSuchElementException("Habitación no encontrada con id: " + id);
                });
    }

    public Habitacion guardar(Habitacion habitacion) {

        log.info("Creando habitación número: {}", habitacion.getNumeroHabitacion());

        validarEstadoHabitacion(habitacion.getEstadoHabitacion());
        log.info("Validando hotel con id: {}", habitacion.getIdHotel());
        hotelClient.obtenerHotelPorId(habitacion.getIdHotel());
        habitacion.setEstadoHabitacion(habitacion.getEstadoHabitacion().toUpperCase());

        Habitacion habitacionGuardada = habitacionRepository.save(habitacion);

        log.info("Habitación creada correctamente con id: {}", habitacionGuardada.getIdHabitacion());
        return habitacionGuardada;
    }

    public Habitacion actualizar(Long id, Habitacion habitacionActualizada) {

        log.info("Actualizando habitación con id: {}", id);
        validarEstadoHabitacion(habitacionActualizada.getEstadoHabitacion());
        log.info("Validando hotel con id: {}", habitacionActualizada.getIdHotel());
        hotelClient.obtenerHotelPorId(habitacionActualizada.getIdHotel());

        Habitacion habitacionExistente = findById(id);

        habitacionExistente.setNumeroHabitacion(habitacionActualizada.getNumeroHabitacion());
        habitacionExistente.setTipoHabitacion(habitacionActualizada.getTipoHabitacion());
        habitacionExistente.setPrecioBase(habitacionActualizada.getPrecioBase());
        habitacionExistente.setCapacidad(habitacionActualizada.getCapacidad());
        habitacionExistente.setEstadoHabitacion(habitacionActualizada.getEstadoHabitacion().toUpperCase());
        habitacionExistente.setIdHotel(habitacionActualizada.getIdHotel());

        Habitacion habitacionGuardada = habitacionRepository.save(habitacionExistente);
        log.info("Habitación actualizada correctamente con id: {}", habitacionGuardada.getIdHabitacion());
        return habitacionGuardada;
    }

    public void eliminar(Long id) {

        log.warn("Eliminando habitación con id: {}", id);
        Habitacion habitacion = findById(id);
        habitacionRepository.delete(habitacion);
        log.info("Habitación eliminada correctamente con id: {}", id);
    }

    public List<Habitacion> findByEstadoHabitacion(String estadoHabitacion) {
        log.info("Buscando habitaciones por estado: {}", estadoHabitacion);
        validarEstadoHabitacion(estadoHabitacion);

        return habitacionRepository.findByEstadoHabitacion(
                estadoHabitacion.toUpperCase()
        );
    }

    public List<Habitacion> findByCapacidadMinima(Integer capacidad) {

        log.info("Buscando habitaciones con capacidad mínima: {}", capacidad);

        return habitacionRepository.findByCapacidadGreaterThanEqual(capacidad);
    }

    public List<Habitacion> findByIdHotel(Long idHotel) {
        log.info("Buscando habitaciones por idHotel: {}", idHotel);
        return habitacionRepository.findByIdHotel(idHotel);
    }

    private void validarEstadoHabitacion(String estadoHabitacion) {

        log.info("Validando estado de habitación: {}", estadoHabitacion);

        if (!estadoHabitacion.equalsIgnoreCase("DISPONIBLE") &&
                !estadoHabitacion.equalsIgnoreCase("OCUPADA") &&
                !estadoHabitacion.equalsIgnoreCase("MANTENIMIENTO")) {
            log.error("Estado de habitación inválido recibido: {}", estadoHabitacion);
            throw new IllegalArgumentException(
                    "Estado de habitación inválido. Use: DISPONIBLE, OCUPADA o MANTENIMIENTO"
            );
        }
    }
}