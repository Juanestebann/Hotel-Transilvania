package com.example.ms_reserva_service.client;

import com.example.ms_reserva_service.dto.HotelDTO;
import com.example.ms_reserva_service.security.TokenProvider;
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

    public HotelDTO obtenerHotelPorId(Long idHotel) {

        return webClientBuilder
                .baseUrl("http://localhost:8083/api/v1/hoteles")
                .build()
                .get()
                .uri("/{id}", idHotel)
                .header(HttpHeaders.AUTHORIZATION, tokenProvider.getAuthorizationHeader())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        Mono.error(new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Hotel no encontrado con id: " + idHotel
                        ))
                )
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new ResponseStatusException(
                                HttpStatus.SERVICE_UNAVAILABLE,
                                "Error al comunicarse con ms-hotel-service"
                        ))
                )
                .bodyToMono(HotelDTO.class)
                .block();
    }
}