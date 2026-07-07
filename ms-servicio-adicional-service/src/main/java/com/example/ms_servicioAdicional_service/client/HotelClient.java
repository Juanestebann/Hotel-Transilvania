package com.example.ms_servicioAdicional_service.client;

import com.example.ms_servicioAdicional_service.dto.HotelDTO;
import com.example.ms_servicioAdicional_service.security.TokenProvider;
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
public class HotelClient {

    private final WebClient.Builder webClientBuilder;
    private final TokenProvider tokenProvider;

    @Value("${hoteles.service.url}")
    private String hotelesServiceUrl;

    public HotelDTO obtenerHotelPorId(Long id) {

        WebClient webClient = webClientBuilder
                .clone()
                .baseUrl(hotelesServiceUrl)
                .build();

        return RemoteCallSupport.block(webClient.get()
                .uri("/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, tokenProvider.getAuthorizationHeader())
                .retrieve()

                // Error 404
                .onStatus(HttpStatusCode::is4xxClientError, response -> {
                    HttpStatusCode status = response.statusCode();
                    return Mono.error(new ResponseStatusException(status,
                            status.value() == 404 ? "Hotel no encontrado con id: " + id
                                    : "Hotel-service rechazo la solicitud con estado " + status.value()));
                })

                // Error 500
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new ResponseStatusException(
                                HttpStatus.SERVICE_UNAVAILABLE,
                                "Error en ms-hotel-service"
                        ))
                )

                .bodyToMono(HotelDTO.class), "hotel-service");
    }
}
