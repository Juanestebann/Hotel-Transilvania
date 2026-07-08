package com.example.ms_resena_service.client;

import com.example.ms_resena_service.dto.HabitacionDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HabitacionClientTest {

    @Test
    void usaUrlConfiguradaYPropagaAuthorization() {
        AtomicReference<ClientRequest> request = new AtomicReference<>();
        HabitacionDTO resultado = client(ClientTestSupport.success(
                request,
                "{\"id\":4,\"idHotel\":3,\"tipoHabitacion\":\"DOBLE\"}"
        )).obtenerHabitacionPorId(4L);

        assertEquals(4L, resultado.getId());
        assertRequest(request.get(), "/api/v1/habitaciones/4");
    }

    @ParameterizedTest
    @CsvSource({"UNAUTHORIZED,UNAUTHORIZED", "FORBIDDEN,FORBIDDEN",
            "NOT_FOUND,NOT_FOUND", "INTERNAL_SERVER_ERROR,SERVICE_UNAVAILABLE"})
    void mapeaEstados(HttpStatus remoto, HttpStatus esperado) {
        ClientTestSupport.assertStatus(esperado,
                () -> client(ClientTestSupport.status(remoto)).obtenerHabitacionPorId(4L));
    }

    @Test
    void respuestaVaciaSeConvierteEnServiceUnavailable() {
        ClientTestSupport.assertStatus(HttpStatus.SERVICE_UNAVAILABLE,
                () -> client(ClientTestSupport.empty()).obtenerHabitacionPorId(4L));
    }

    @Test
    void fallaConexionSeConvierteEnServiceUnavailable() {
        ClientTestSupport.assertStatus(HttpStatus.SERVICE_UNAVAILABLE,
                () -> client(ClientTestSupport.connectionFailure(
                        "http://configured.test/api/v1/habitaciones/4"
                )).obtenerHabitacionPorId(4L));
    }

    private void assertRequest(ClientRequest request, String path) {
        assertEquals("configured.test", request.url().getHost());
        assertEquals(path, request.url().getPath());
        assertEquals("Bearer token-user", request.headers().getFirst(HttpHeaders.AUTHORIZATION));
    }

    private HabitacionClient client(WebClient.Builder builder) {
        return ClientTestSupport.configured(
                new HabitacionClient(builder, ClientTestSupport.tokenProvider()),
                "habitacionesServiceUrl",
                "http://configured.test/api/v1/habitaciones"
        );
    }
}
