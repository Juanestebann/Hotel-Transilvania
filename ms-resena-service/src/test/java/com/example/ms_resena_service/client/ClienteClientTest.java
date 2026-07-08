package com.example.ms_resena_service.client;

import com.example.ms_resena_service.dto.ClienteDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClienteClientTest {

    @Test
    void usaUrlConfiguradaYPropagaAuthorization() {
        AtomicReference<ClientRequest> request = new AtomicReference<>();
        ClienteDTO resultado = client(ClientTestSupport.success(
                request,
                "{\"id\":2,\"rutDocumento\":\"11-1\",\"tipoCliente\":\"REGULAR\"}"
        )).obtenerClientePorId(2L);

        assertEquals(2L, resultado.getId());
        assertRequest(request.get(), "/api/v1/clientes/2");
    }

    @ParameterizedTest
    @CsvSource({"UNAUTHORIZED,UNAUTHORIZED", "FORBIDDEN,FORBIDDEN",
            "NOT_FOUND,NOT_FOUND", "INTERNAL_SERVER_ERROR,SERVICE_UNAVAILABLE"})
    void mapeaEstados(HttpStatus remoto, HttpStatus esperado) {
        ClientTestSupport.assertStatus(esperado,
                () -> client(ClientTestSupport.status(remoto)).obtenerClientePorId(2L));
    }

    @Test
    void respuestaVaciaSeConvierteEnServiceUnavailable() {
        ClientTestSupport.assertStatus(HttpStatus.SERVICE_UNAVAILABLE,
                () -> client(ClientTestSupport.empty()).obtenerClientePorId(2L));
    }

    @Test
    void fallaConexionSeConvierteEnServiceUnavailable() {
        ClientTestSupport.assertStatus(HttpStatus.SERVICE_UNAVAILABLE,
                () -> client(ClientTestSupport.connectionFailure(
                        "http://configured.test/api/v1/clientes/2"
                )).obtenerClientePorId(2L));
    }

    private void assertRequest(ClientRequest request, String path) {
        assertEquals("configured.test", request.url().getHost());
        assertEquals(path, request.url().getPath());
        assertEquals("Bearer token-user", request.headers().getFirst(HttpHeaders.AUTHORIZATION));
    }

    private ClienteClient client(WebClient.Builder builder) {
        return ClientTestSupport.configured(
                new ClienteClient(builder, ClientTestSupport.tokenProvider()),
                "clientesServiceUrl",
                "http://configured.test/api/v1/clientes"
        );
    }
}
