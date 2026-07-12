package com.example.apigateway.filter;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CorrelationIdGlobalFilterTest {

    private static final String AUTHORIZATION_VALUE = "Bearer token-super-secreto";

    private final CorrelationIdGlobalFilter filter = new CorrelationIdGlobalFilter();

    @Test
    void generaCorrelationIdCuandoNoVieneEnRequest() {
        MockServerWebExchange exchange = exchange(MockServerHttpRequest.get("/api/v1/hoteles"));
        AtomicReference<ServerWebExchange> downstream = new AtomicReference<>();

        filter.filter(exchange, successfulChain(downstream, HttpStatus.OK)).block();

        String generatedId = downstream.get().getRequest().getHeaders()
                .getFirst(CorrelationIdGlobalFilter.CORRELATION_ID_HEADER);

        assertNotNull(generatedId);
        assertDoesNotThrow(() -> UUID.fromString(generatedId));
        assertEquals(generatedId, exchange.getResponse().getHeaders()
                .getFirst(CorrelationIdGlobalFilter.CORRELATION_ID_HEADER));
    }

    @Test
    void conservaCorrelationIdRecibido() {
        String correlationId = "correlation-existente-123";
        MockServerWebExchange exchange = exchange(MockServerHttpRequest.get("/api/v1/reservas")
                .header(CorrelationIdGlobalFilter.CORRELATION_ID_HEADER, correlationId));
        AtomicReference<ServerWebExchange> downstream = new AtomicReference<>();

        filter.filter(exchange, successfulChain(downstream, HttpStatus.OK)).block();

        assertEquals(correlationId, downstream.get().getRequest().getHeaders()
                .getFirst(CorrelationIdGlobalFilter.CORRELATION_ID_HEADER));
        assertEquals(correlationId, exchange.getResponse().getHeaders()
                .getFirst(CorrelationIdGlobalFilter.CORRELATION_ID_HEADER));
    }

    @Test
    void permiteContinuarLaCadenaSinCambiarStatus() {
        MockServerWebExchange exchange = exchange(MockServerHttpRequest.post("/api/v1/pagos"));
        AtomicBoolean invoked = new AtomicBoolean();
        GatewayFilterChain chain = correlatedExchange -> {
            invoked.set(true);
            correlatedExchange.getResponse().setStatusCode(HttpStatus.CREATED);
            return correlatedExchange.getResponse().setComplete();
        };

        filter.filter(exchange, chain).block();

        assertTrue(invoked.get());
        assertEquals(HttpStatus.CREATED, exchange.getResponse().getStatusCode());
    }

    @Test
    void agregaHeaderAunqueDownstreamRespondaError() {
        MockServerWebExchange exchange = exchange(MockServerHttpRequest.get("/api/v1/clientes"));
        AtomicReference<ServerWebExchange> downstream = new AtomicReference<>();
        GatewayFilterChain chain = correlatedExchange -> {
            downstream.set(correlatedExchange);
            correlatedExchange.getResponse().setStatusCode(HttpStatus.BAD_GATEWAY);
            return Mono.error(new IllegalStateException("downstream error"));
        };

        assertThrows(IllegalStateException.class, () -> filter.filter(exchange, chain).block());

        String correlationId = downstream.get().getRequest().getHeaders()
                .getFirst(CorrelationIdGlobalFilter.CORRELATION_ID_HEADER);
        assertNotNull(correlationId);
        assertEquals(correlationId, exchange.getResponse().getHeaders()
                .getFirst(CorrelationIdGlobalFilter.CORRELATION_ID_HEADER));
        assertEquals(HttpStatus.BAD_GATEWAY, exchange.getResponse().getStatusCode());
    }

    @Test
    void noRequiereAuthorization() {
        MockServerWebExchange exchange = exchange(MockServerHttpRequest.get("/api/v1/hoteles/1"));
        AtomicReference<ServerWebExchange> downstream = new AtomicReference<>();

        filter.filter(exchange, successfulChain(downstream, HttpStatus.OK)).block();

        assertNull(downstream.get().getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
        assertEquals(HttpStatus.OK, exchange.getResponse().getStatusCode());
    }

    @Test
    void noRegistraNiExponeAuthorization() {
        Logger logger = (Logger) LoggerFactory.getLogger(CorrelationIdGlobalFilter.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        try {
            MockServerWebExchange exchange = exchange(MockServerHttpRequest.get("/api/v1/usuarios/me")
                    .header(HttpHeaders.AUTHORIZATION, AUTHORIZATION_VALUE));
            AtomicReference<ServerWebExchange> downstream = new AtomicReference<>();

            filter.filter(exchange, successfulChain(downstream, HttpStatus.OK)).block();

            assertEquals(AUTHORIZATION_VALUE, downstream.get().getRequest().getHeaders()
                    .getFirst(HttpHeaders.AUTHORIZATION));
            assertNull(exchange.getResponse().getHeaders().getFirst(HttpHeaders.AUTHORIZATION));

            String logs = appender.list.stream()
                    .map(ILoggingEvent::getFormattedMessage)
                    .reduce("", (left, right) -> left + right);

            assertTrue(logs.contains("correlationId="));
            assertTrue(logs.contains("method=GET"));
            assertTrue(logs.contains("path=/api/v1/usuarios/me"));
            assertTrue(logs.contains("status=200"));
            assertTrue(logs.contains("durationMs="));
            assertFalse(logs.contains("Authorization"));
            assertFalse(logs.contains(AUTHORIZATION_VALUE));
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }

    private MockServerWebExchange exchange(MockServerHttpRequest.BaseBuilder<?> request) {
        return MockServerWebExchange.from(request.build());
    }

    private GatewayFilterChain successfulChain(
            AtomicReference<ServerWebExchange> downstream,
            HttpStatus status
    ) {
        return exchange -> {
            downstream.set(exchange);
            exchange.getResponse().setStatusCode(status);
            return exchange.getResponse().setComplete();
        };
    }
}
