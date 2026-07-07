package com.example.ms_resena_service.client;

import com.example.ms_resena_service.dto.ReservaDTO;
import com.example.ms_resena_service.security.TokenProvider;
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
public class ReservaClient {

    private static final String RESERVAS_BASE_URL = "http://localhost:8086/api/v1/reservas";

    private final WebClient.Builder webClientBuilder;
    private final TokenProvider tokenProvider;

    public ReservaDTO obtenerReservaPorId(Long idReserva) {
        return webClientBuilder
                .baseUrl(RESERVAS_BASE_URL)
                .build()
                .get()
                .uri("/{id}", idReserva)
                .header(HttpHeaders.AUTHORIZATION, tokenProvider.getAuthorizationHeader())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> {
                    HttpStatusCode status = response.statusCode();
                    String mensaje = status.value() == HttpStatus.NOT_FOUND.value()
                            ? "Reserva no encontrada con id: " + idReserva
                            : "Reserva-service rechazó la consulta con estado " + status.value();

                    return Mono.error(new ResponseStatusException(status, mensaje));
                })
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new ResponseStatusException(
                                HttpStatus.SERVICE_UNAVAILABLE,
                                "Reserva-service no está disponible"
                        ))
                )
                .bodyToMono(ReservaDTO.class)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "Reserva-service devolvió una respuesta vacía"
                )))
                .onErrorMap(WebClientRequestException.class, exception ->
                        new ResponseStatusException(
                                HttpStatus.SERVICE_UNAVAILABLE,
                                "No fue posible comunicarse con reserva-service",
                                exception
                        ))
                .block();
    }
}
