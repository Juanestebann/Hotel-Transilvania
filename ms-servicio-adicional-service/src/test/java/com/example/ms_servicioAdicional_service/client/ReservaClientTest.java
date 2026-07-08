package com.example.ms_servicioAdicional_service.client;

import com.example.ms_servicioAdicional_service.dto.ReservaDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReservaClientTest {

    @Test
    void usaUrlConfiguradaYPropagaAuthorization() {
        AtomicReference<ClientRequest> request = new AtomicReference<>();
        ReservaDTO resultado = client(ClientTestSupport.success(
                request,
                "{\"id\":5,\"estadoReserva\":\"CONFIRMADA\"}"
        )).obtenerReservaPorId(5L);

        assertEquals(5L, resultado.getId());
        assertRequest(request.get(), "/api/v1/reservas/5");
    }

    @ParameterizedTest
    @CsvSource({"UNAUTHORIZED,UNAUTHORIZED", "FORBIDDEN,FORBIDDEN",
            "NOT_FOUND,NOT_FOUND", "INTERNAL_SERVER_ERROR,SERVICE_UNAVAILABLE"})
    void mapeaEstados(HttpStatus remoto, HttpStatus esperado) {
        ClientTestSupport.assertStatus(esperado,
                () -> client(ClientTestSupport.status(remoto)).obtenerReservaPorId(5L));
    }

    @Test
    void respuestaVaciaSeConvierteEnServiceUnavailable() {
        ClientTestSupport.assertStatus(HttpStatus.SERVICE_UNAVAILABLE,
                () -> client(ClientTestSupport.empty()).obtenerReservaPorId(5L));
    }

    @Test
    void fallaConexionSeConvierteEnServiceUnavailable() {
        ClientTestSupport.assertStatus(HttpStatus.SERVICE_UNAVAILABLE,
                () -> client(ClientTestSupport.connectionFailure(
                        "http://configured.test/api/v1/reservas/5"
                )).obtenerReservaPorId(5L));
    }

    private void assertRequest(ClientRequest request, String path) {
        assertEquals("configured.test", request.url().getHost());
        assertEquals(path, request.url().getPath());
        assertEquals("Bearer token-user", request.headers().getFirst(HttpHeaders.AUTHORIZATION));
    }

    private ReservaClient client(WebClient.Builder builder) {
        return ClientTestSupport.configured(
                new ReservaClient(builder, ClientTestSupport.tokenProvider()),
                "reservasServiceUrl",
                "http://configured.test/api/v1/reservas"
        );
    }
}
