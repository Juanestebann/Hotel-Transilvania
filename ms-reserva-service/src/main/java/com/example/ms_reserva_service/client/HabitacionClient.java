package com.example.ms_reserva_service.client;

import com.example.ms_reserva_service.dto.HabitacionDTO;
import com.example.ms_reserva_service.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${habitaciones.service.url}")
    private String habitacionesServiceUrl;

    public HabitacionDTO obtenerHabitacionPorId(Long id) {

        WebClient webClient = webClientBuilder
                .clone()
                .baseUrl(habitacionesServiceUrl)
                .build();

        return RemoteCallSupport.block(webClient.get()
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
                .bodyToMono(HabitacionDTO.class), "habitacion-service");
    }
}
