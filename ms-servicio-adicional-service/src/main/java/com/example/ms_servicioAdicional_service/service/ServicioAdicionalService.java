package com.example.ms_servicioAdicional_service.service;

import com.example.ms_servicioAdicional_service.client.HotelClient;
import com.example.ms_servicioAdicional_service.client.ReservaClient;
import com.example.ms_servicioAdicional_service.model.ServicioAdicionalModel;
import com.example.ms_servicioAdicional_service.repository.ServicioAdicionalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServicioAdicionalService {

    private final ServicioAdicionalRepository servicioAdicionalRepository;
    private final HotelClient hotelClient;
    private final ReservaClient reservaClient;

    public List<ServicioAdicionalModel> findAll() {
        log.info("Listando todos los servicios adicionales");
        return servicioAdicionalRepository.findAll();
    }

    public ServicioAdicionalModel findById(Long id) {
        log.info("Buscando servicio adicional con id: {}", id);

        return servicioAdicionalRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Servicio adicional no encontrado con id: {}", id);
                    return new NoSuchElementException("Servicio adicional no encontrado con id: " + id);
                });
    }

    public List<ServicioAdicionalModel> findByIdHotel(Long idHotel) {
        log.info("Buscando servicios adicionales por hotel id: {}", idHotel);
        return servicioAdicionalRepository.findByIdHotel(idHotel);
    }

    public List<ServicioAdicionalModel> findByIdReserva(Long idReserva) {
        log.info("Buscando servicios adicionales por reserva id: {}", idReserva);
        return servicioAdicionalRepository.findByIdReserva(idReserva);
    }

    public List<ServicioAdicionalModel> findByEstado(String estado) {
        log.info("Buscando servicios adicionales por estado: {}", estado);
        return servicioAdicionalRepository.findByEstado(estado);
    }

    public List<ServicioAdicionalModel> findByNombre(String nombre) {
        log.info("Buscando servicios adicionales por nombre: {}", nombre);
        return servicioAdicionalRepository.findByNombre(nombre);
    }

    public ServicioAdicionalModel guardar(ServicioAdicionalModel servicioAdicional) {

        log.info(
                "Intentando crear servicio adicional: {} para hotel id: {} y reserva id: {}",
                servicioAdicional.getNombre(),
                servicioAdicional.getIdHotel(),
                servicioAdicional.getIdReserva()
        );

        log.info("Validando existencia de hotel id: {}", servicioAdicional.getIdHotel());
        hotelClient.obtenerHotelPorId(servicioAdicional.getIdHotel());
        log.info("Hotel validado correctamente con id: {}", servicioAdicional.getIdHotel());

        if (servicioAdicional.getIdReserva() != null) {

            log.info(
                    "Validando existencia de reserva id: {}",
                    servicioAdicional.getIdReserva()
            );

            reservaClient.obtenerReservaPorId(servicioAdicional.getIdReserva());

            log.info(
                    "Reserva validada correctamente con id: {}",
                    servicioAdicional.getIdReserva()
            );

        } else {

            log.info(
                    "El servicio adicional no fue asociado a una reserva específica"
            );
        }

        ServicioAdicionalModel servicioGuardado =
                servicioAdicionalRepository.save(servicioAdicional);

        log.info(
                "Servicio adicional creado correctamente con id: {}",
                servicioGuardado.getId()
        );

        return servicioGuardado;
    }

    public ServicioAdicionalModel actualizar(
            Long id,
            ServicioAdicionalModel servicioActualizado
    ) {

        log.info("Actualizando servicio adicional con id: {}", id);

        ServicioAdicionalModel servicioExistente = findById(id);

        log.info(
                "Validando existencia de hotel id: {}",
                servicioActualizado.getIdHotel()
        );

        hotelClient.obtenerHotelPorId(servicioActualizado.getIdHotel());

        log.info(
                "Hotel validado correctamente con id: {}",
                servicioActualizado.getIdHotel()
        );

        if (servicioActualizado.getIdReserva() != null) {

            log.info(
                    "Validando existencia de reserva id: {}",
                    servicioActualizado.getIdReserva()
            );

            reservaClient.obtenerReservaPorId(
                    servicioActualizado.getIdReserva()
            );

            log.info(
                    "Reserva validada correctamente con id: {}",
                    servicioActualizado.getIdReserva()
            );

        } else {

            log.info(
                    "El servicio adicional actualizado no fue asociado a una reserva específica"
            );
        }

        servicioExistente.setIdHotel(servicioActualizado.getIdHotel());
        servicioExistente.setIdReserva(servicioActualizado.getIdReserva());
        servicioExistente.setNombre(servicioActualizado.getNombre());
        servicioExistente.setDescripcion(servicioActualizado.getDescripcion());
        servicioExistente.setPrecio(servicioActualizado.getPrecio());
        servicioExistente.setEstado(servicioActualizado.getEstado());

        ServicioAdicionalModel servicioGuardado =
                servicioAdicionalRepository.save(servicioExistente);

        log.info(
                "Servicio adicional actualizado correctamente con id: {}",
                servicioGuardado.getId()
        );

        return servicioGuardado;
    }

    public void eliminar(Long id) {

        log.warn(
                "Solicitando eliminación de servicio adicional con id: {}",
                id
        );

        ServicioAdicionalModel servicio = findById(id);

        servicioAdicionalRepository.delete(servicio);

        log.warn(
                "Servicio adicional eliminado correctamente con id: {}",
                id
        );
    }
}