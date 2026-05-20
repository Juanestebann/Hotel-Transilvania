package com.example.ms_notificacion_service.client;

import com.example.ms_notificacion_service.dto.UsuarioDTO;
import com.example.ms_notificacion_service.security.TokenProvider;
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
public class UsuarioClient {

    private final WebClient.Builder webClientBuilder;
    private final TokenProvider tokenProvider;

    public UsuarioDTO obtenerUsuarioPorId(Long id) {

        WebClient webClient = webClientBuilder
                .baseUrl("http://localhost:8081/api/v1/usuarios")
                .build();

        return webClient.get()
                .uri("/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, tokenProvider.getAuthorizationHeader())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        Mono.error(new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Usuario no encontrado con id: " + id
                        ))
                )
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new ResponseStatusException(
                                HttpStatus.SERVICE_UNAVAILABLE,
                                "Error en ms-auth-usuarios-service"
                        ))
                )
                .bodyToMono(UsuarioDTO.class)
                .block();
    }
}
