package com.example.ms_reserva_service.client;

import com.example.ms_reserva_service.dto.DisponibilidadDTO;
import com.example.ms_reserva_service.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DisponibilidadClient {

    private final WebClient.Builder webClientBuilder;
    private final TokenProvider tokenProvider;

    public DisponibilidadDTO obtenerDisponibilidadPorHabitacionYFecha(
            Long idHabitacion,
            LocalDate fecha
    ) {

        WebClient webClient = webClientBuilder
                .baseUrl("http://localhost:8085/api/v1/disponibilidades")
                .build();

        return webClient.get()
                .uri("/habitacion/{idHabitacion}/fecha/{fecha}", idHabitacion, fecha)
                .header(HttpHeaders.AUTHORIZATION, tokenProvider.getAuthorizationHeader())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        Mono.error(new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "No existe disponibilidad para la habitación "
                                        + idHabitacion + " en la fecha " + fecha
                        ))
                )
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new ResponseStatusException(
                                HttpStatus.SERVICE_UNAVAILABLE,
                                "Error en ms-disponibilidad-service"
                        ))
                )
                .bodyToMono(DisponibilidadDTO.class)
                .block();
    }

    public DisponibilidadDTO actualizarDisponibilidad(
            Long idDisponibilidad,
            DisponibilidadDTO disponibilidad
    ) {

        WebClient webClient = webClientBuilder
                .baseUrl("http://localhost:8085/api/v1/disponibilidades")
                .build();

        return webClient.put()
                .uri("/{id}", idDisponibilidad)
                .header(HttpHeaders.AUTHORIZATION, tokenProvider.getAuthorizationHeader())
                .bodyValue(disponibilidad)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        Mono.error(new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "No se pudo actualizar la disponibilidad"
                        ))
                )
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new ResponseStatusException(
                                HttpStatus.SERVICE_UNAVAILABLE,
                                "Error en ms-disponibilidad-service"
                        ))
                )
                .bodyToMono(DisponibilidadDTO.class)
                .block();
    }
}