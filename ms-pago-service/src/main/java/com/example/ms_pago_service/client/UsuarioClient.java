package com.example.ms_pago_service.client;

import com.example.ms_pago_service.dto.UsuarioDTO;
import lombok.RequiredArgsConstructor;
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

    public UsuarioDTO obtenerUsuarioPorId(Long idUsuario) {

        return webClientBuilder
                .baseUrl("http://localhost:8081/api/v1/usuarios")
                .build()
                .get()
                .uri("/{id}", idUsuario)
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
                                "Error al comunicarse con ms-auth-usuarios-service"
                        ))
                )
                .bodyToMono(UsuarioDTO.class)
                .block();
    }
}