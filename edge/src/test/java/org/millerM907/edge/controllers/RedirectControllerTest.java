package org.millerM907.edge.controllers;

import org.millerM907.edge.models.RedirectDecision;
import org.millerM907.edge.services.RedirectService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@WebFluxTest(controllers = RedirectController.class)
@DisplayName("RedirectController — unit tests")
class RedirectControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private RedirectService redirectService;

    @Nested
    @DisplayName("Given redirect decision without method preservation")
    class Given302 {

        @Test
        @DisplayName("When GET /s/{slug} is called then responds 302 with Location header")
        void givenDecision302_whenGet_thenFoundAndLocation() {
            String url = "http://localhost:8082/landing/chrome";
            when(redirectService.resolve(any(), any()))
                    .thenReturn(Mono.just(new RedirectDecision(url, false, 60, "OK")));

            webTestClient.get().uri("/s/{slug}", "sale1111")
                    .exchange()
                    .expectStatus().isEqualTo(HttpStatus.FOUND)
                    .expectHeader().valueEquals("Location", url);
        }
    }

    @Nested
    @DisplayName("Given redirect decision with method preservation")
    class Given307 {

        @Test
        @DisplayName("When GET /s/{slug} is called then responds 307 with Location header")
        void givenDecision307_whenGet_thenTemporaryRedirectAndLocation() {
            String url = "http://localhost:8082/landing/firefox";
            when(redirectService.resolve(any(), any()))
                    .thenReturn(Mono.just(new RedirectDecision(url, true, 60, "OK")));

            webTestClient.get().uri("/s/{slug}", "sale1111")
                    .exchange()
                    .expectStatus().isEqualTo(HttpStatus.TEMPORARY_REDIRECT)
                    .expectHeader().valueEquals("Location", url);
        }
    }

    @Nested
    @DisplayName("Given service throws an error")
    class GivenServiceError {

        @Test
        @DisplayName("When GET /s/{slug} is called then responds with 5xx")
        void givenServiceError_whenGet_then5xx() {
            when(redirectService.resolve(any(), any()))
                    .thenReturn(Mono.error(new RuntimeException("boom")));

            webTestClient.get().uri("/s/{slug}", "broken")
                    .exchange()
                    .expectStatus().is5xxServerError();
        }
    }

    @Nested
    @DisplayName("Given a valid request")
    class GivenValidRequest {

        @Test
        @DisplayName("When GET /s/{slug} is called then slug and exchange are passed to service")
        void givenValidRequest_whenGet_thenSlugAndExchangePassed() {
            String url = "http://localhost:8082/landing/default";
            ArgumentCaptor<String> slugCap = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<org.springframework.web.server.ServerWebExchange> exchCap =
                    ArgumentCaptor.forClass(org.springframework.web.server.ServerWebExchange.class);

            when(redirectService.resolve(any(), any()))
                    .thenAnswer(inv -> Mono.just(new RedirectDecision(url, false, 60, "OK")));

            webTestClient.get().uri("/s/{slug}", "sale1111")
                    .exchange()
                    .expectStatus().isEqualTo(HttpStatus.FOUND)
                    .expectHeader().valueEquals("Location", url);

            verify(redirectService, times(1)).resolve(slugCap.capture(), exchCap.capture());
            assertThat(slugCap.getValue()).isEqualTo("sale1111");
            assertThat(exchCap.getValue()).isNotNull();
        }
    }
}
