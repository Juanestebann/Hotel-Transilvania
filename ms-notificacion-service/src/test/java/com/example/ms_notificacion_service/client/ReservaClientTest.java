package com.example.ms_notificacion_service.client;

import com.example.ms_notificacion_service.dto.ReservaDTO;
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
                "{\"id\":3,\"idUsuario\":7,\"estadoReserva\":\"CONFIRMADA\"}"
        )).obtenerReservaPorId(3L);

        assertEquals(3L, resultado.getId());
        assertRequest(request.get(), "/api/v1/reservas/3");
    }

    @ParameterizedTest
    @CsvSource({"UNAUTHORIZED,UNAUTHORIZED", "FORBIDDEN,FORBIDDEN",
            "NOT_FOUND,NOT_FOUND", "INTERNAL_SERVER_ERROR,SERVICE_UNAVAILABLE"})
    void mapeaEstados(HttpStatus remoto, HttpStatus esperado) {
        ClientTestSupport.assertStatus(esperado,
                () -> client(ClientTestSupport.status(remoto)).obtenerReservaPorId(3L));
    }

    @Test
    void respuestaVaciaSeConvierteEnServiceUnavailable() {
        ClientTestSupport.assertStatus(HttpStatus.SERVICE_UNAVAILABLE,
                () -> client(ClientTestSupport.empty()).obtenerReservaPorId(3L));
    }

    @Test
    void fallaConexionSeConvierteEnServiceUnavailable() {
        ClientTestSupport.assertStatus(HttpStatus.SERVICE_UNAVAILABLE,
                () -> client(ClientTestSupport.connectionFailure(
                        "http://configured.test/api/v1/reservas/3"
                )).obtenerReservaPorId(3L));
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
