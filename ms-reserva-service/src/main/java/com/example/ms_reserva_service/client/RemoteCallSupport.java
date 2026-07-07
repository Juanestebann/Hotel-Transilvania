package com.example.ms_reserva_service.client;

import io.netty.handler.timeout.ReadTimeoutException;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

final class RemoteCallSupport {
    private static final Duration TOTAL_TIMEOUT = Duration.ofSeconds(6);

    private RemoteCallSupport() {
    }

    static <T> T block(Mono<T> response, String serviceName) {
        return response
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                        serviceName + " devolvio una respuesta vacia")))
                .timeout(TOTAL_TIMEOUT)
                .onErrorMap(RemoteCallSupport::isTimeout, exception -> new ResponseStatusException(
                        HttpStatus.GATEWAY_TIMEOUT,
                        "Tiempo de espera agotado al comunicarse con " + serviceName,
                        exception))
                .onErrorMap(WebClientRequestException.class, exception -> new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "No fue posible comunicarse con " + serviceName,
                        exception))
                .block();
    }

    private static boolean isTimeout(Throwable exception) {
        Throwable current = exception;
        while (current != null) {
            if (current instanceof TimeoutException || current instanceof ReadTimeoutException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
