package com.example.ms_disponibilidad_service.client;

import com.example.ms_disponibilidad_service.dto.HabitacionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class HabitacionClient {

    private final WebClient.Builder webClientBuilder;

    public HabitacionDTO obtenerHabitacionPorId(Long id) {

        WebClient webClient = webClientBuilder
                .baseUrl("http://localhost:8084/api/v1/habitaciones")
                .build();

        return webClient.get()
                .uri("/{id}", id)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        Mono.error(new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Habitación no encontrada con id: " + id
                        ))
                )
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new ResponseStatusException(
                                HttpStatus.SERVICE_UNAVAILABLE,
                                "Error en ms-habitacion-service"
                        ))
                )
                .bodyToMono(HabitacionDTO.class)
                .block();
    }
}