package com.example.ms_pago_service.client;

import com.example.ms_pago_service.dto.ReservaDTO;
import com.example.ms_pago_service.security.ServiceTokenProvider;
import com.example.ms_pago_service.security.TokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
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
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReservaClientTest {

    private static final String SECRET = "mi_clave_super_secreta_para_jwt_123456";

    @Test
    void consultaReservaConTokenDelUsuario() {
        AtomicReference<ClientRequest> solicitud = new AtomicReference<>();
        WebClient.Builder builder = respuestaExitosa(solicitud);

        new ReservaClient(builder, tokenProvider(), serviceTokenProviderMock())
                .obtenerReservaPorId(1L);

        assertEquals("/api/v1/reservas/1", solicitud.get().url().getPath());
        assertEquals("Bearer token-user",
                solicitud.get().headers().getFirst(HttpHeaders.AUTHORIZATION));
    }

    @Test
    void cambiaEstadoConTokenServiceYEndpointInterno() {
        AtomicReference<ClientRequest> solicitud = new AtomicReference<>();
        WebClient.Builder builder = respuestaExitosa(solicitud);

        new ReservaClient(builder, tokenProvider(), serviceTokenProviderMock())
                .cambiarEstadoReserva(1L, "CONFIRMADA");

        assertEquals(HttpMethod.PUT, solicitud.get().method());
        assertEquals("/api/v1/reservas/internal/1/estado", solicitud.get().url().getPath());
        assertEquals("CONFIRMADA", solicitud.get().url().getQuery().split("=")[1]);
        assertEquals("Bearer token-service",
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
                () -> new ReservaClient(builder, tokenProvider(), serviceTokenProviderMock())
                        .cambiarEstadoReserva(1L, "CONFIRMADA")
        );

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    }

    @Test
    void mapeaFallaDeConexionAServiceUnavailable() {
        WebClientRequestException errorConexion = new WebClientRequestException(
                new IOException("conexión rechazada"),
                HttpMethod.PUT,
                URI.create("http://localhost:8086/api/v1/reservas/internal/1/estado"),
                HttpHeaders.EMPTY
        );
        WebClient.Builder builder = WebClient.builder().exchangeFunction(request ->
                Mono.error(errorConexion)
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> new ReservaClient(builder, tokenProvider(), serviceTokenProviderMock())
                        .cambiarEstadoReserva(1L, "CONFIRMADA")
        );

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    }

    @Test
    void serviceTokenRepresentaPagoYExpiraPronto() {
        String authorization = new ServiceTokenProvider(SECRET).getAuthorizationHeader();
        String token = authorization.substring("Bearer ".length());

        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals("ms-pago-service", claims.getSubject());
        assertEquals("SERVICE", claims.get("rol", String.class));
        assertTrue(claims.getExpiration().toInstant().isAfter(Instant.now()));
        assertTrue(claims.getExpiration().toInstant().isBefore(Instant.now().plusSeconds(180)));
    }

    private void assertErrorRemoto(HttpStatus estadoRemoto, HttpStatus estadoEsperado) {
        WebClient.Builder builder = WebClient.builder().exchangeFunction(request ->
                Mono.just(ClientResponse.create(estadoRemoto).build())
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> new ReservaClient(builder, tokenProvider(), serviceTokenProviderMock())
                        .cambiarEstadoReserva(1L, "CONFIRMADA")
        );

        assertEquals(estadoEsperado, exception.getStatusCode());
    }

    private WebClient.Builder respuestaExitosa(AtomicReference<ClientRequest> solicitud) {
        return WebClient.builder().exchangeFunction(request -> {
            solicitud.set(request);
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body("{\"id\":1,\"idUsuario\":7,\"estadoReserva\":\"CONFIRMADA\"}")
                    .build());
        });
    }

    private TokenProvider tokenProvider() {
        TokenProvider tokenProvider = mock(TokenProvider.class);
        when(tokenProvider.getAuthorizationHeader()).thenReturn("Bearer token-user");
        return tokenProvider;
    }

    private ServiceTokenProvider serviceTokenProviderMock() {
        ServiceTokenProvider serviceTokenProvider = mock(ServiceTokenProvider.class);
        when(serviceTokenProvider.getAuthorizationHeader()).thenReturn("Bearer token-service");
        return serviceTokenProvider;
    }
}
