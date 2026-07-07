package com.example.ms_resena_service.client;

import com.example.ms_resena_service.dto.HabitacionDTO;
import com.example.ms_resena_service.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
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
                    return Mono.error(new ResponseStatusException(status,
                            status.value() == 404 ? "Habitacion no encontrada con id: " + id
                                    : "Habitacion-service rechazo la solicitud con estado " + status.value()));
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
