package com.example.ms_reserva_service.client;

import com.example.ms_reserva_service.dto.ClienteDTO;
import lombok.RequiredArgsConstructor;
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

    public Mono<ClienteDTO> obtenerCliente(Long id){

        WebClient webClient = webClientBuilder
                .baseUrl("http://localhost:8082/api/v1/clientes")
                .build();

        return webClient.get()
                .uri("/{id}", id)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        Mono.error(new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Cliente no encontrado con id: " + id
                        ))
                )
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new ResponseStatusException(
                                HttpStatus.SERVICE_UNAVAILABLE,
                                "Error en ms-cliente-service"
                        ))
                )
                .bodyToMono(ClienteDTO.class);
    }
}
