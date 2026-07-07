package com.example.ms_resena_service.client;

import com.example.ms_resena_service.dto.ReservaDTO;
import com.example.ms_resena_service.security.TokenProvider;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReservaClientTest {

    @Test
    void obtieneReservaYReenviaAuthorization() {
        AtomicReference<ClientRequest> solicitud = new AtomicReference<>();
        WebClient.Builder builder = WebClient.builder().exchangeFunction(request -> {
            solicitud.set(request);
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body("{\"id\":1,\"idUsuario\":7,\"idCliente\":2,\"idHotel\":3,\"idHabitacion\":4,\"estadoReserva\":\"FINALIZADA\"}")
                    .build());
        });

        ReservaDTO resultado = client(builder).obtenerReservaPorId(1L);

        assertEquals(7L, resultado.getIdUsuario());
        assertEquals(2L, resultado.getIdCliente());
        assertEquals("/api/v1/reservas/1", solicitud.get().url().getPath());
        assertEquals("configured.test", solicitud.get().url().getHost());
        assertEquals("Bearer token-user",
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
                () -> client(builder).obtenerReservaPorId(1L)
        );

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    }

    @Test
    void mapeaFallaDeConexionAServiceUnavailable() {
        WebClientRequestException errorConexion = new WebClientRequestException(
                new IOException("conexión rechazada"),
                HttpMethod.GET,
                URI.create("http://localhost:8086/api/v1/reservas/1"),
                HttpHeaders.EMPTY
        );
        WebClient.Builder builder = WebClient.builder().exchangeFunction(request ->
                Mono.error(errorConexion)
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> client(builder).obtenerReservaPorId(1L)
        );

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    }

    private void assertErrorRemoto(HttpStatus estadoRemoto, HttpStatus estadoEsperado) {
        WebClient.Builder builder = WebClient.builder().exchangeFunction(request ->
                Mono.just(ClientResponse.create(estadoRemoto).build())
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> client(builder).obtenerReservaPorId(1L)
        );

        assertEquals(estadoEsperado, exception.getStatusCode());
    }

    private TokenProvider tokenProvider() {
        TokenProvider tokenProvider = mock(TokenProvider.class);
        when(tokenProvider.getAuthorizationHeader()).thenReturn("Bearer token-user");
        return tokenProvider;
    }

    private ReservaClient client(WebClient.Builder builder) {
        return ClientTestSupport.configured(new ReservaClient(builder, tokenProvider()),
                "reservasServiceUrl", "http://configured.test/api/v1/reservas");
    }
}
