package com.example.ms_reserva_service.client;

import com.example.ms_reserva_service.dto.DisponibilidadDTO;
import com.example.ms_reserva_service.security.ServiceTokenProvider;
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

import java.time.LocalDate;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DisponibilidadClient {

    private static final String DISPONIBILIDADES_BASE_URL =
            "http://localhost:8085/api/v1/disponibilidades";

    private final WebClient.Builder webClientBuilder;
    private final TokenProvider tokenProvider;
    private final ServiceTokenProvider serviceTokenProvider;

    public DisponibilidadDTO obtenerDisponibilidadPorHabitacionYFecha(
            Long idHabitacion,
            LocalDate fecha
    ) {

        WebClient webClient = webClientBuilder
                .baseUrl(DISPONIBILIDADES_BASE_URL)
                .build();

        return webClient.get()
                .uri("/habitacion/{idHabitacion}/fecha/{fecha}", idHabitacion, fecha)
                .header(HttpHeaders.AUTHORIZATION, tokenProvider.getAuthorizationHeader())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> {
                    HttpStatusCode status = response.statusCode();
                    String mensaje = status.value() == HttpStatus.NOT_FOUND.value()
                            ? "No existe disponibilidad para la habitación "
                            + idHabitacion + " en la fecha " + fecha
                            : "Disponibilidad-service rechazó la lectura con estado " + status.value();

                    return Mono.error(new ResponseStatusException(status, mensaje));
                })
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new ResponseStatusException(
                                HttpStatus.SERVICE_UNAVAILABLE,
                                "Disponibilidad-service no está disponible"
                        ))
                )
                .bodyToMono(DisponibilidadDTO.class)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "Disponibilidad-service devolvió una respuesta vacía"
                )))
                .onErrorMap(WebClientRequestException.class, exception ->
                        new ResponseStatusException(
                                HttpStatus.SERVICE_UNAVAILABLE,
                                "No fue posible comunicarse con disponibilidad-service",
                                exception
                        ))
                .block();
    }

    public DisponibilidadDTO actualizarDisponibilidad(
            Long idDisponibilidad,
            DisponibilidadDTO disponibilidad
    ) {

        WebClient webClient = webClientBuilder
                .baseUrl(DISPONIBILIDADES_BASE_URL)
                .build();

        return webClient.put()
                .uri("/internal/{id}", idDisponibilidad)
                .header(HttpHeaders.AUTHORIZATION, serviceTokenProvider.getAuthorizationHeader())
                .bodyValue(Map.of("estado", disponibilidad.getEstado()))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> {
                    HttpStatusCode status = response.statusCode();
                    return Mono.error(new ResponseStatusException(
                            status,
                            "Disponibilidad-service rechazó la actualización con estado " + status.value()
                    ));
                })
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new ResponseStatusException(
                                HttpStatus.SERVICE_UNAVAILABLE,
                                "Disponibilidad-service no está disponible"
                        ))
                )
                .bodyToMono(DisponibilidadDTO.class)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "Disponibilidad-service devolvió una respuesta vacía"
                )))
                .onErrorMap(WebClientRequestException.class, exception ->
                        new ResponseStatusException(
                                HttpStatus.SERVICE_UNAVAILABLE,
                                "No fue posible comunicarse con disponibilidad-service",
                                exception
                        ))
                .block();
    }
}
