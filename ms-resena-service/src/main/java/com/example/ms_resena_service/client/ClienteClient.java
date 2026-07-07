package com.example.ms_resena_service.client;

import com.example.ms_resena_service.dto.ClienteDTO;
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
public class ClienteClient {

    private final WebClient.Builder webClientBuilder;
    private final TokenProvider tokenProvider;

    @Value("${clientes.service.url}")
    private String clientesServiceUrl;

    public ClienteDTO obtenerClientePorId(Long id) {

        WebClient webClient = webClientBuilder
                .clone()
                .baseUrl(clientesServiceUrl)
                .build();

        return RemoteCallSupport.block(webClient.get()
                .uri("/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, tokenProvider.getAuthorizationHeader())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> {
                    HttpStatusCode status = response.statusCode();
                    return Mono.error(new ResponseStatusException(status,
                            status.value() == 404 ? "Cliente no encontrado con id: " + id
                                    : "Cliente-service rechazo la solicitud con estado " + status.value()));
                })
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new ResponseStatusException(
                                HttpStatus.SERVICE_UNAVAILABLE,
                                "Error en ms-cliente-service"
                        ))
                )
                .bodyToMono(ClienteDTO.class), "cliente-service");
    }
}
