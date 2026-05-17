package com.example.ms_pago_service.client;
import com.example.ms_pago_service.dto.ReservaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ReservaClient {

    private final WebClient.Builder webClientBuilder;

    public ReservaDTO obtenerReservaPorId(Long idReserva) {

        return webClientBuilder
                .baseUrl("http://localhost:8086/api/v1/reservas")
                .build()
                .get()
                .uri("/{id}", idReserva)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        Mono.error(new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Reserva no encontrada con id: " + idReserva
                        ))
                )
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new ResponseStatusException(
                                HttpStatus.SERVICE_UNAVAILABLE,
                                "Error al comunicarse con ms-reserva-service"
                        ))
                )
                .bodyToMono(ReservaDTO.class)
                .block();
    }
    public ReservaDTO cambiarEstadoReserva(Long idReserva, String estadoReserva) {

        return webClientBuilder
                .baseUrl("http://localhost:8086/api/v1/reservas")
                .build()
                .put()
                .uri("/{id}/estado?estadoReserva={estado}", idReserva, estadoReserva)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        Mono.error(new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "No se pudo actualizar la reserva con id: " + idReserva
                        ))
                )
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new ResponseStatusException(
                                HttpStatus.SERVICE_UNAVAILABLE,
                                "Error al comunicarse con ms-reserva-service"
                        ))
                )
                .bodyToMono(ReservaDTO.class)
                .block();
    }
}
