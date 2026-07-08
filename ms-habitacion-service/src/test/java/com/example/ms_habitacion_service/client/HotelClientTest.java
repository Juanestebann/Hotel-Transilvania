package com.example.ms_habitacion_service.client;

import com.example.ms_habitacion_service.dto.HotelDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HotelClientTest {

    @Test
    void usaUrlConfiguradaYPropagaAuthorization() {
        AtomicReference<ClientRequest> request = new AtomicReference<>();

        HotelDTO resultado = client(ClientTestSupport.success(
                request,
                "{\"id\":1,\"nombre\":\"Hotel Test\"}"
        )).obtenerHotelPorId(1L);

        assertEquals(1L, resultado.getId());
        assertEquals("Hotel Test", resultado.getNombre());
        assertEquals("configured.test", request.get().url().getHost());
        assertEquals("/api/v1/hoteles/1", request.get().url().getPath());
        assertEquals("Bearer token-user",
                request.get().headers().getFirst(HttpHeaders.AUTHORIZATION));
    }

    @ParameterizedTest
    @CsvSource({
            "UNAUTHORIZED,UNAUTHORIZED",
            "FORBIDDEN,FORBIDDEN",
            "NOT_FOUND,NOT_FOUND",
            "INTERNAL_SERVER_ERROR,SERVICE_UNAVAILABLE"
    })
    void conservaErroresClienteYMapeaErrorServidor(
            HttpStatus estadoRemoto,
            HttpStatus estadoEsperado
    ) {
        ClientTestSupport.assertStatus(
                estadoEsperado,
                () -> client(ClientTestSupport.status(estadoRemoto)).obtenerHotelPorId(1L)
        );
    }

    @Test
    void respuestaVaciaSeConvierteEnServiceUnavailable() {
        ClientTestSupport.assertStatus(
                HttpStatus.SERVICE_UNAVAILABLE,
                () -> client(ClientTestSupport.empty()).obtenerHotelPorId(1L)
        );
    }

    @Test
    void fallaConexionSeConvierteEnServiceUnavailable() {
        ClientTestSupport.assertStatus(
                HttpStatus.SERVICE_UNAVAILABLE,
                () -> client(ClientTestSupport.connectionFailure(
                        "http://configured.test/api/v1/hoteles/1"
                )).obtenerHotelPorId(1L)
        );
    }

    private HotelClient client(WebClient.Builder builder) {
        return ClientTestSupport.configured(
                new HotelClient(builder, ClientTestSupport.tokenProvider()),
                "hotelesServiceUrl",
                "http://configured.test/api/v1/hoteles"
        );
    }
}
