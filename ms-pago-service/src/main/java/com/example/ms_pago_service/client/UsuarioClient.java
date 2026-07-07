package com.example.ms_pago_service.client;

import com.example.ms_pago_service.dto.UsuarioDTO;
import com.example.ms_pago_service.security.TokenProvider;
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
public class UsuarioClient {

    private static final String USUARIOS_BASE_URL = "http://localhost:8081/api/v1/usuarios";

    private final WebClient.Builder webClientBuilder;
    private final TokenProvider tokenProvider;

    public UsuarioDTO obtenerUsuarioActual() {
        return ejecutarConsulta("/me", "Usuario autenticado no encontrado");
    }

    public UsuarioDTO obtenerUsuarioPorId(Long idUsuario) {
        return ejecutarConsulta("/" + idUsuario, "Usuario no encontrado con id: " + idUsuario);
    }

    private UsuarioDTO ejecutarConsulta(String uri, String mensajeNoEncontrado) {
        return webClientBuilder
                .baseUrl(USUARIOS_BASE_URL)
                .build()
                .get()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, tokenProvider.getAuthorizationHeader())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> {
                    HttpStatusCode status = response.statusCode();
                    String mensaje = status.value() == HttpStatus.NOT_FOUND.value()
                            ? mensajeNoEncontrado
                            : "Auth-service rechazó la solicitud con estado " + status.value();

                    return Mono.error(new ResponseStatusException(status, mensaje));
                })
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new ResponseStatusException(
                                HttpStatus.SERVICE_UNAVAILABLE,
                                "Auth-service no está disponible"
                        ))
                )
                .bodyToMono(UsuarioDTO.class)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "Auth-service devolvió una respuesta vacía"
                )))
                .onErrorMap(WebClientRequestException.class, exception ->
                        new ResponseStatusException(
                                HttpStatus.SERVICE_UNAVAILABLE,
                                "No fue posible comunicarse con auth-service",
                                exception
                        ))
                .block();
    }
}
