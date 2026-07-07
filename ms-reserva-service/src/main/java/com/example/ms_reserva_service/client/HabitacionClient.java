package com.example.ms_reserva_service.client;

import com.example.ms_reserva_service.dto.HabitacionDTO;
import com.example.ms_reserva_service.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class HabitacionClient {

    private final WebClient.Builder webClientBuilder;
    private final TokenProvider tokenProvider;

    public HabitacionDTO obtenerHabitacionPorId(Long id) {

        WebClient webClient = webClientBuilder
                .baseUrl("http://localhost:8084/api/v1/habitaciones")
                .build();

        return webClient.get()
                .uri("/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, tokenProvider.getAuthorizationHeader())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> {
                    HttpStatusCode status = response.statusCode();
                    String mensaje = status.value() == HttpStatus.NOT_FOUND.value()
                            ? "Habitación no encontrada con id: " + id
                            : "Habitación-service rechazó la solicitud con estado " + status.value();

                    return Mono.error(new ResponseStatusException(status, mensaje));
                })
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new ResponseStatusException(
                                HttpStatus.SERVICE_UNAVAILABLE,
                                "Error en ms-habitacion-service"
                        ))
                )
                .bodyToMono(HabitacionDTO.class)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "Habitación-service devolvió una respuesta vacía"
                )))
                .onErrorMap(WebClientRequestException.class, exception ->
                        new ResponseStatusException(
                                HttpStatus.SERVICE_UNAVAILABLE,
                                "No fue posible comunicarse con habitación-service",
                                exception
                        ))
                .block();
    }
}
