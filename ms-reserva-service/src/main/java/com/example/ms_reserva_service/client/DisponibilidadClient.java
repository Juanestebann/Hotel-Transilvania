package com.example.ms_reserva_service.client;

import com.example.ms_reserva_service.dto.DisponibilidadDTO;
import com.example.ms_reserva_service.security.ServiceTokenProvider;
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

import java.time.LocalDate;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DisponibilidadClient {

    private final WebClient.Builder webClientBuilder;
    private final ServiceTokenProvider serviceTokenProvider;

    @Value("${disponibilidades.service.url}")
    private String disponibilidadesServiceUrl;

    public DisponibilidadDTO obtenerDisponibilidadPorHabitacionYFecha(
            Long idHabitacion,
            LocalDate fecha
    ) {

        WebClient webClient = webClientBuilder
                .clone()
                .baseUrl(disponibilidadesServiceUrl)
                .build();

        return RemoteCallSupport.block(webClient.get()
                .uri("/internal/habitacion/{idHabitacion}/fecha/{fecha}", idHabitacion, fecha)
                .header(HttpHeaders.AUTHORIZATION, serviceTokenProvider.getAuthorizationHeader())
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
                .bodyToMono(DisponibilidadDTO.class), "disponibilidad-service");
    }

    public DisponibilidadDTO actualizarDisponibilidad(
            Long idDisponibilidad,
            DisponibilidadDTO disponibilidad
    ) {

        WebClient webClient = webClientBuilder
                .clone()
                .baseUrl(disponibilidadesServiceUrl)
                .build();

        return RemoteCallSupport.block(webClient.put()
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
                .bodyToMono(DisponibilidadDTO.class), "disponibilidad-service");
    }
}
