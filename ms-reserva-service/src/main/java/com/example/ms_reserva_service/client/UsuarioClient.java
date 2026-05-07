package com.example.ms_reserva_service.client;

import com.example.ms_reserva_service.dto.UsuarioDTO;
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
        return webClientBuilder.build()
                .get()
                .uri("http://localhost:8081/api/v1/usuarios/{id}", idUsuario)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        Mono.error(new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Usuario no encontrado con id: " + idUsuario
                        ))
                )
                .bodyToMono(UsuarioDTO.class)
                .block();
    }
}
