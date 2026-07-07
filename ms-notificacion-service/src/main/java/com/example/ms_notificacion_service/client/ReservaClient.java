package com.example.ms_notificacion_service.client;

import com.example.ms_notificacion_service.dto.ReservaDTO;
import com.example.ms_notificacion_service.security.TokenProvider;
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
public class ReservaClient {

    private final WebClient.Builder webClientBuilder;
    private final TokenProvider tokenProvider;

    @Value("${reservas.service.url}")
    private String reservasServiceUrl;

    public ReservaDTO obtenerReservaPorId(Long id) {

        WebClient webClient = webClientBuilder
                .clone()
                .baseUrl(reservasServiceUrl)
                .build();

        return RemoteCallSupport.block(webClient.get()
                .uri("/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, tokenProvider.getAuthorizationHeader())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> {
                    HttpStatusCode status = response.statusCode();
                    return Mono.error(new ResponseStatusException(status,
                            status.value() == 404 ? "Reserva no encontrada con id: " + id
                                    : "Reserva-service rechazo la solicitud con estado " + status.value()));
                })
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new ResponseStatusException(
                                HttpStatus.SERVICE_UNAVAILABLE,
                                "Error en ms-reserva-service"
                        ))
                )
                .bodyToMono(ReservaDTO.class), "reserva-service");
    }
}
