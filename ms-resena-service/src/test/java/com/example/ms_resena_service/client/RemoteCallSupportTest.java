package com.example.ms_resena_service.client;

import io.netty.handler.timeout.ReadTimeoutException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RemoteCallSupportTest {
    @Test
    void mapeaTimeoutDeLecturaAGatewayTimeout() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> RemoteCallSupport.block(Mono.error(ReadTimeoutException.INSTANCE), "reserva-service"));
        assertEquals(HttpStatus.GATEWAY_TIMEOUT, exception.getStatusCode());
    }
}
