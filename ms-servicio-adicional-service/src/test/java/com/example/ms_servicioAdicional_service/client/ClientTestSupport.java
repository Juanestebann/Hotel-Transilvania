package com.example.ms_servicioAdicional_service.client;

import com.example.ms_servicioAdicional_service.security.TokenProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
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

final class ClientTestSupport {
    private ClientTestSupport() {
    }

    static <T> T configured(T client, String field, String baseUrl) {
        ReflectionTestUtils.setField(client, field, baseUrl);
        return client;
    }

    static TokenProvider tokenProvider() {
        TokenProvider tokenProvider = mock(TokenProvider.class);
        when(tokenProvider.getAuthorizationHeader()).thenReturn("Bearer token-user");
        return tokenProvider;
    }

    static WebClient.Builder success(AtomicReference<ClientRequest> request, String body) {
        return WebClient.builder().exchangeFunction(clientRequest -> {
            request.set(clientRequest);
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .body(body)
                    .build());
        });
    }

    static WebClient.Builder status(HttpStatus status) {
        return WebClient.builder().exchangeFunction(request ->
                Mono.just(ClientResponse.create(status).build()));
    }

    static WebClient.Builder empty() {
        return WebClient.builder().exchangeFunction(request ->
                Mono.just(ClientResponse.create(HttpStatus.NO_CONTENT).build()));
    }

    static WebClient.Builder connectionFailure(String url) {
        WebClientRequestException exception = new WebClientRequestException(
                new IOException("conexión rechazada"),
                HttpMethod.GET,
                URI.create(url),
                HttpHeaders.EMPTY
        );
        return WebClient.builder().exchangeFunction(request -> Mono.error(exception));
    }

    static void assertStatus(HttpStatus expected, Runnable invocation) {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, invocation::run);
        assertEquals(expected, exception.getStatusCode());
    }
}
