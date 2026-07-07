package com.example.ms_reserva_service.client;

import com.example.ms_reserva_service.dto.ClienteValidacionDTO;
import com.example.ms_reserva_service.security.TokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ClienteClientTest {

    @Test
    void validaClienteEnEndpointMinimoYReenviaAuthorization() {
        AtomicReference<ClientRequest> solicitud = new AtomicReference<>();
        WebClient.Builder builder = WebClient.builder().exchangeFunction(request -> {
            solicitud.set(request);
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body("{\"idCliente\":7,\"existe\":true}")
                    .build());
        });

        ClienteValidacionDTO resultado =
                new ClienteClient(builder, tokenProvider()).obtenerClientePorId(7L);

        assertEquals(7L, resultado.idCliente());
        assertTrue(resultado.existe());
        assertEquals("/api/v1/clientes/7/validacion", solicitud.get().url().getPath());
        assertEquals("Bearer token-test",
                solicitud.get().headers().getFirst(HttpHeaders.AUTHORIZATION));
    }

    @Test
    void conservaUnauthorized() {
        assertErrorRemoto(HttpStatus.UNAUTHORIZED, HttpStatus.UNAUTHORIZED);
    }

    @Test
    void conservaForbidden() {
        assertErrorRemoto(HttpStatus.FORBIDDEN, HttpStatus.FORBIDDEN);
    }

    @Test
    void conservaNotFound() {
        assertErrorRemoto(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND);
    }

    @Test
    void mapeaErrorRemotoAServiceUnavailable() {
        assertErrorRemoto(HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    void mapeaRespuestaVaciaAServiceUnavailable() {
        WebClient.Builder builder = WebClient.builder().exchangeFunction(request ->
                Mono.just(ClientResponse.create(HttpStatus.NO_CONTENT).build())
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> new ClienteClient(builder, tokenProvider()).obtenerClientePorId(1L)
        );

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    }

    @Test
    void mapeaFallaDeConexionAServiceUnavailable() {
        WebClientRequestException errorConexion = new WebClientRequestException(
                new IOException("conexión rechazada"),
                HttpMethod.GET,
                URI.create("http://localhost:8082/api/v1/clientes/1/validacion"),
                HttpHeaders.EMPTY
        );
        WebClient.Builder builder = WebClient.builder().exchangeFunction(request ->
                Mono.error(errorConexion)
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> new ClienteClient(builder, tokenProvider()).obtenerClientePorId(1L)
        );

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    }

    private void assertErrorRemoto(HttpStatus estadoRemoto, HttpStatus estadoEsperado) {
        WebClient.Builder builder = WebClient.builder().exchangeFunction(request ->
                Mono.just(ClientResponse.create(estadoRemoto).build())
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> new ClienteClient(builder, tokenProvider()).obtenerClientePorId(1L)
        );

        assertEquals(estadoEsperado, exception.getStatusCode());
    }

    private TokenProvider tokenProvider() {
        TokenProvider tokenProvider = mock(TokenProvider.class);
        when(tokenProvider.getAuthorizationHeader()).thenReturn("Bearer token-test");
        return tokenProvider;
    }
}
