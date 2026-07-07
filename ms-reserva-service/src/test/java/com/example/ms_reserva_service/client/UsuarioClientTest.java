package com.example.ms_reserva_service.client;

import com.example.ms_reserva_service.dto.UsuarioDTO;
import com.example.ms_reserva_service.security.TokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UsuarioClientTest {

    @Test
    void obtenerUsuarioActualReenviaAuthorizationYLeeDtoSeguro() {
        AtomicReference<ClientRequest> solicitud = new AtomicReference<>();
        TokenProvider tokenProvider = tokenProvider();
        WebClient.Builder builder = WebClient.builder().exchangeFunction(request -> {
            solicitud.set(request);
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body("{\"idUsuario\":7,\"nombre\":\"mavis\",\"rol\":\"USER\"}")
                    .build());
        });

        UsuarioDTO resultado = new UsuarioClient(builder, tokenProvider).obtenerUsuarioActual();

        assertEquals(7L, resultado.getIdUsuario());
        assertEquals("mavis", resultado.getNombre());
        assertEquals("USER", resultado.getRol());
        assertEquals("/api/v1/usuarios/me", solicitud.get().url().getPath());
        assertEquals("Bearer token-test",
                solicitud.get().headers().getFirst(HttpHeaders.AUTHORIZATION));
    }

    @Test
    void obtenerUsuarioActualConservaUnauthorized() {
        assertErrorRemoto(HttpStatus.UNAUTHORIZED, HttpStatus.UNAUTHORIZED);
    }

    @Test
    void obtenerUsuarioActualConservaForbidden() {
        assertErrorRemoto(HttpStatus.FORBIDDEN, HttpStatus.FORBIDDEN);
    }

    @Test
    void obtenerUsuarioActualConservaNotFound() {
        assertErrorRemoto(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND);
    }

    @Test
    void obtenerUsuarioActualMapeaErrorRemotoAServiceUnavailable() {
        assertErrorRemoto(HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.SERVICE_UNAVAILABLE);
    }

    private void assertErrorRemoto(HttpStatus estadoRemoto, HttpStatus estadoEsperado) {
        WebClient.Builder builder = WebClient.builder().exchangeFunction(request ->
                Mono.just(ClientResponse.create(estadoRemoto).build())
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> new UsuarioClient(builder, tokenProvider()).obtenerUsuarioActual()
        );

        assertEquals(estadoEsperado, exception.getStatusCode());
    }

    private TokenProvider tokenProvider() {
        TokenProvider tokenProvider = mock(TokenProvider.class);
        when(tokenProvider.getAuthorizationHeader()).thenReturn("Bearer token-test");
        return tokenProvider;
    }
}
