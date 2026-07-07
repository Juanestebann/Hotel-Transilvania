package com.example.ms_reserva_service.client;

import com.example.ms_reserva_service.dto.HotelDTO;
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
                .onStatus(HttpStatusCode::is4xxClientError, response -> {
                    HttpStatusCode status = response.statusCode();
                    String mensaje = status.value() == HttpStatus.NOT_FOUND.value()
                            ? "Hotel no encontrado con id: " + idHotel
                            : "Hotel-service rechazó la solicitud con estado " + status.value();

                    return Mono.error(new ResponseStatusException(status, mensaje));
                })
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new ResponseStatusException(
                                HttpStatus.SERVICE_UNAVAILABLE,
                                "Error al comunicarse con ms-hotel-service"
                        ))
                )
                .bodyToMono(HotelDTO.class)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "Hotel-service devolvió una respuesta vacía"
                )))
                .onErrorMap(WebClientRequestException.class, exception ->
                        new ResponseStatusException(
                                HttpStatus.SERVICE_UNAVAILABLE,
                                "No fue posible comunicarse con hotel-service",
                                exception
                        ))
                .block();
    }
}
