package com.example.ms_reserva_service.client;

import com.example.ms_reserva_service.dto.UsuarioDTO;
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
public class UsuarioClient {

    private final WebClient.Builder webClientBuilder;
    private final TokenProvider tokenProvider;

    public UsuarioDTO obtenerUsuarioPorId(Long idUsuario) {

        WebClient webClient = webClientBuilder
                .baseUrl("http://localhost:8081/api/v1/usuarios")
                .build();

        return webClient.get()
                .uri("/{id}", idUsuario)
                .header(HttpHeaders.AUTHORIZATION, tokenProvider.getAuthorizationHeader())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        Mono.error(new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Usuario no encontrado con id: " + idUsuario
                        ))
                )
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new ResponseStatusException(
                                HttpStatus.SERVICE_UNAVAILABLE,
                                "Error en ms-usuario-service"
                        ))
                )
                .bodyToMono(UsuarioDTO.class)
                .block();
    }
}