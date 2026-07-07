package com.example.ms_reserva_service.client;

import com.example.ms_reserva_service.dto.ClienteValidacionDTO;
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
public class ClienteClient {

    private static final String CLIENTES_BASE_URL = "http://localhost:8082/api/v1/clientes";

    private final WebClient.Builder webClientBuilder;
    private final TokenProvider tokenProvider;

    public ClienteValidacionDTO obtenerClientePorId(Long id) {

        WebClient webClient = webClientBuilder
                .baseUrl(CLIENTES_BASE_URL)
                .build();

        return webClient.get()
                .uri("/{id}/validacion", id)
                .header(HttpHeaders.AUTHORIZATION, tokenProvider.getAuthorizationHeader())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> {
                    HttpStatusCode status = response.statusCode();
                    String mensaje = status.value() == HttpStatus.NOT_FOUND.value()
                            ? "Cliente no encontrado con id: " + id
                            : "Cliente-service rechazó la solicitud con estado " + status.value();

                    return Mono.error(new ResponseStatusException(status, mensaje));
                })
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new ResponseStatusException(
                                HttpStatus.SERVICE_UNAVAILABLE,
                                "Cliente-service no está disponible"
                        ))
                )
                .bodyToMono(ClienteValidacionDTO.class)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "Cliente-service devolvió una respuesta vacía"
                )))
                .onErrorMap(WebClientRequestException.class, exception ->
                        new ResponseStatusException(
                                HttpStatus.SERVICE_UNAVAILABLE,
                                "No fue posible comunicarse con cliente-service",
                                exception
                        ))
                .block();
    }
}
