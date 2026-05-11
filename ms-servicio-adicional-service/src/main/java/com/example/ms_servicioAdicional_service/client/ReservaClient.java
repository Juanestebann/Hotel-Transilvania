package com.example.ms_servicioAdicional_service.client;

import com.example.ms_servicioAdicional_service.dto.ReservaDTO;
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

    public ReservaDTO obtenerReservaPorId(Long id) {

        WebClient webClient = webClientBuilder
                .baseUrl("http://localhost:8086/api/v1/reservas")
                .build();

        return webClient.get()
                .uri("/{id}", id)
                .retrieve()

                // Error 404
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        Mono.error(new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Reserva no encontrada con id: " + id
                        ))
                )

                // Error 500
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new ResponseStatusException(
                                HttpStatus.SERVICE_UNAVAILABLE,
                                "Error en ms-reserva-service"
                        ))
                )

                .bodyToMono(ReservaDTO.class)
                .block();
    }
}