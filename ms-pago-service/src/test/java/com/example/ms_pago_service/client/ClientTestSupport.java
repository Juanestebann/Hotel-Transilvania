package com.example.ms_pago_service.client;

import org.springframework.test.util.ReflectionTestUtils;

final class ClientTestSupport {
    private ClientTestSupport() {
    }

    static <T> T configured(T client, String field, String baseUrl) {
        ReflectionTestUtils.setField(client, field, baseUrl);
        return client;
    }
}
