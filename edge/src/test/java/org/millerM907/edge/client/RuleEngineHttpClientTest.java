package org.millerM907.edge.client;

import org.millerM907.edge.client.impl.RuleEngineHttpClient;
import org.millerM907.edge.models.RequestContext;
import org.millerM907.edge.models.ResolveRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.lang.reflect.Field;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RuleEngineHttpClient — unit tests")
class RuleEngineHttpClientTest {

    private RuleEngineHttpClient client;

    @BeforeEach
    void setUp() {
        client = new RuleEngineHttpClient("http://unused");
    }

    private ResolveRequest sample() {
        return new ResolveRequest(
                "sale1111",
                new RequestContext(Map.of("browser", "Chrome", "device", "mobile", "time", "10:15"))
        );
    }

    private void swapExchange(java.util.function.Function<ClientRequest, Mono<ClientResponse>> fn) throws Exception {
        Field f = RuleEngineHttpClient.class.getDeclaredField("client");
        f.setAccessible(true);
        f.set(client, WebClient.builder().exchangeFunction(req -> {
            assertThat(req.method().name()).isEqualTo("POST");
            assertThat(req.url().getPath()).isEqualTo("/resolve");
            assertThat(req.headers().getFirst(HttpHeaders.CONTENT_TYPE))
                    .startsWith(MediaType.APPLICATION_JSON_VALUE);
            return fn.apply(req);
        }).build());
    }

    @Nested
    @DisplayName("Given a successful response (200)")
    class GivenSuccess200 {

        @Test
        @DisplayName("When resolve is invoked then body is deserialized")
        void given200_whenResolve_thenBodyDeserialized() throws Exception {
            String json = """
                      {"targetUrl":"http://localhost:8082/landing/ok","ttlSeconds":60,"reason":"OK"}
                    """;
            swapExchange(req -> Mono.just(ClientResponse.create(HttpStatusCode.valueOf(200))
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(json)
                    .build()));

            StepVerifier.create(client.resolve(sample()))
                    .expectNextMatches(r ->
                            r.targetUrl().equals("http://localhost:8082/landing/ok") &&
                                    r.ttlSeconds() == 60 &&
                                    r.reason().equals("OK"))
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("Given a client error (400)")
    class Given400 {

        @Test
        @DisplayName("When resolve is invoked then WebClientResponseException.BadRequest is thrown")
        void given400_whenResolve_thenBadRequest() throws Exception {
            swapExchange(req -> Mono.just(ClientResponse.create(HttpStatusCode.valueOf(400))
                    .body("bad request")
                    .build()));

            StepVerifier.create(client.resolve(sample()))
                    .expectError(WebClientResponseException.BadRequest.class)
                    .verify();
        }
    }

    @Nested
    @DisplayName("Given a server error (503)")
    class Given503 {

        @Test
        @DisplayName("When resolve is invoked then WebClientResponseException.ServiceUnavailable is thrown")
        void given503_whenResolve_thenServiceUnavailable() throws Exception {
            swapExchange(req -> Mono.just(ClientResponse.create(HttpStatusCode.valueOf(503))
                    .body("unavailable")
                    .build()));

            StepVerifier.create(client.resolve(sample()))
                    .expectError(WebClientResponseException.ServiceUnavailable.class)
                    .verify();
        }
    }

    @Nested
    @DisplayName("Given invalid JSON in 200 response")
    class GivenInvalidJson {

        @Test
        @DisplayName("When resolve is invoked then decoding error is propagated")
        void givenInvalidJson_whenResolve_thenDecodingError() throws Exception {
            swapExchange(req -> Mono.just(ClientResponse.create(HttpStatusCode.valueOf(200))
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body("{ not-json }")
                    .build()));

            StepVerifier.create(client.resolve(sample()))
                    .expectErrorMatches(e ->
                            e.getClass().getName().contains("DecodingException") ||
                                    (e.getCause() != null && e.getCause().getClass().getName().contains("MismatchedInputException")))
                    .verify();
        }
    }

    @Nested
    @DisplayName("Given a 204 No Content response")
    class Given204NoContent {

        @Test
        @DisplayName("When resolve is invoked then Mono completes empty")
        void given204_whenResolve_thenEmpty() throws Exception {
            swapExchange(req -> Mono.just(ClientResponse.create(HttpStatusCode.valueOf(204)).build()));

            StepVerifier.create(client.resolve(sample()))
                    .verifyComplete();
        }
    }
}
