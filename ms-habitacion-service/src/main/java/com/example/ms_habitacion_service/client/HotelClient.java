package com.example.ms_habitacion_service.client;

import com.example.ms_habitacion_service.dto.HotelDTO;
import com.example.ms_habitacion_service.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class HotelClient {

    private final WebClient.Builder webClientBuilder;
    private final TokenProvider tokenProvider;

    public HotelDTO obtenerHotelPorId(Long id) {

        WebClient webClient = webClientBuilder
                .baseUrl("http://localhost:8083/api/v1/hoteles")
                .build();

        return webClient.get()
                .uri("/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, tokenProvider.getAuthorizationHeader())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        Mono.error(new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Hotel no encontrado con id: " + id
                        ))
                )
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new ResponseStatusException(
                                HttpStatus.SERVICE_UNAVAILABLE,
                                "Error en ms-hotel-service"
                        ))
                )
                .bodyToMono(HotelDTO.class)
                .block();
    }
}
