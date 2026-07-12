package com.example.apigateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class CorrelationIdGlobalFilter implements GlobalFilter, Ordered {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    private static final Logger log = LoggerFactory.getLogger(CorrelationIdGlobalFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String correlationId = resolveCorrelationId(exchange);
        long startNanos = System.nanoTime();

        ServerHttpRequest request = exchange.getRequest()
                .mutate()
                .headers(headers -> headers.set(CORRELATION_ID_HEADER, correlationId))
                .build();

        ServerWebExchange correlatedExchange = exchange.mutate()
                .request(request)
                .build();

        correlatedExchange.getResponse().getHeaders()
                .set(CORRELATION_ID_HEADER, correlationId);

        return chain.filter(correlatedExchange)
                .doFinally(signalType -> logRequest(correlatedExchange, correlationId, startNanos));
    }

    private String resolveCorrelationId(ServerWebExchange exchange) {
        String currentValue = exchange.getRequest().getHeaders()
                .getFirst(CORRELATION_ID_HEADER);

        return StringUtils.hasText(currentValue)
                ? currentValue
                : UUID.randomUUID().toString();
    }

    private void logRequest(
            ServerWebExchange exchange,
            String correlationId,
            long startNanos
    ) {
        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
        HttpStatusCode status = exchange.getResponse().getStatusCode();

        log.info(
                "Gateway request correlationId={} method={} path={} status={} durationMs={}",
                correlationId,
                exchange.getRequest().getMethod(),
                exchange.getRequest().getPath().value(),
                status == null ? "UNRESOLVED" : status.value(),
                durationMs
        );
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
