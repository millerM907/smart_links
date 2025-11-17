package org.millerM907.edge.services;

import org.millerM907.edge.client.base.RuleEnginePort;
import org.millerM907.edge.core.cache.ResolutionCache;
import org.millerM907.edge.core.context_aggregator.RequestContextAggregator;
import org.millerM907.edge.core.fingerprint.FingerprintStrategy;
import org.millerM907.edge.models.RedirectDecision;
import org.millerM907.edge.models.RequestContext;
import org.millerM907.edge.models.Resolution;
import org.millerM907.edge.models.ResolveRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("DefaultRedirectService — unit tests")
class DefaultRedirectServiceTest {

    private final FingerprintStrategy fingerprint = mock(FingerprintStrategy.class);
    private final ResolutionCache cache = mock(ResolutionCache.class);
    private final RuleEnginePort ruleEngine = mock(RuleEnginePort.class);
    private final RequestContextAggregator aggregator = mock(RequestContextAggregator.class);

    private final DefaultRedirectService service =
            new DefaultRedirectService(fingerprint, cache, ruleEngine, aggregator);

    private static RequestContext ctx() {
        return new RequestContext(Map.of("browser", "Chrome", "device", "mobile"));
    }

    private static ServerWebExchange exchange() {
        return mock(ServerWebExchange.class, RETURNS_DEEP_STUBS);
    }

    @Nested
    @DisplayName("Given a cached resolution")
    class GivenCacheHit {

        @Test
        @DisplayName("When resolve is called then returns cached URL and does not call rule engine")
        void givenCacheHit_whenResolve_thenReturnsCachedAndSkipsRule() {
            when(aggregator.aggregate(any())).thenReturn(ctx());
            when(fingerprint.fingerprint(eq("slug"), any())).thenReturn("fp");
            when(cache.get("fp")).thenReturn("http://cached");

            Mono<RedirectDecision> mono = service.resolve("slug", exchange());

            StepVerifier.create(mono)
                    .assertNext(d -> {
                        assertThat(d.targetUrl()).isEqualTo("http://cached");
                        assertThat(d.preserveMethod()).isFalse();
                        assertThat(d.ttlSeconds()).isZero();
                        assertThat(d.reason()).isEqualTo("CACHE_HIT");
                    })
                    .verifyComplete();

            verify(ruleEngine, never()).resolve(any());
            verify(cache, never()).put(anyString(), anyString(), anyInt());
        }

        @Test
        @DisplayName("When cached URL is empty string then it is returned as-is")
        void givenCacheHitEmpty_whenResolve_thenReturnsEmptyUrl() {
            when(aggregator.aggregate(any())).thenReturn(ctx());
            when(fingerprint.fingerprint(eq("slug"), any())).thenReturn("fp2");
            when(cache.get("fp2")).thenReturn("");

            StepVerifier.create(service.resolve("slug", exchange()))
                    .assertNext(d -> Assertions.assertThat(d.targetUrl()).isEqualTo(""))
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("Given a cache miss")
    class GivenCacheMiss {

        @Test
        @DisplayName("When rule engine returns resolution then result is mapped and cached with ttl")
        void givenMiss_whenRuleReturns_thenCachesAndReturnsDecision() {
            when(aggregator.aggregate(any())).thenReturn(ctx());
            when(fingerprint.fingerprint(eq("sale1111"), any())).thenReturn("fp3");
            when(cache.get("fp3")).thenReturn(null);
            Resolution res = new Resolution("http://target", 42, "RULE_MATCH");
            when(ruleEngine.resolve(any(ResolveRequest.class))).thenReturn(Mono.just(res));

            StepVerifier.create(service.resolve("sale1111", exchange()))
                    .assertNext(d -> {
                        Assertions.assertThat(d.targetUrl()).isEqualTo("http://target");
                        Assertions.assertThat(d.preserveMethod()).isFalse();
                        Assertions.assertThat(d.ttlSeconds()).isEqualTo(42);
                        Assertions.assertThat(d.reason()).isEqualTo("RULE_MATCH");
                    })
                    .verifyComplete();

            verify(ruleEngine, times(1)).resolve(argThat(rr ->
                    rr.slug().equals("sale1111") && rr.context().attrs().get("browser").equals("Chrome")));
            verify(cache).put("fp3", "http://target", 42);
        }

        @Test
        @DisplayName("When rule engine fails then error is propagated")
        void givenMiss_whenRuleErrors_thenPropagates() {
            when(aggregator.aggregate(any())).thenReturn(ctx());
            when(fingerprint.fingerprint(eq("slug"), any())).thenReturn("fp4");
            when(cache.get("fp4")).thenReturn(null);
            when(ruleEngine.resolve(any())).thenReturn(Mono.error(new IllegalStateException("boom")));

            StepVerifier.create(service.resolve("slug", exchange()))
                    .expectErrorSatisfies(e -> assertThat(e)
                            .isInstanceOf(IllegalStateException.class)
                            .hasMessageContaining("boom"))
                    .verify();

            verify(cache, never()).put(anyString(), anyString(), anyInt());
        }
    }

    @Nested
    @DisplayName("Given aggregator and fingerprint interactions")
    class GivenInteractions {

        @Test
        @DisplayName("When resolve is called then aggregator and fingerprint receive correct arguments")
        void givenResolve_whenCalled_thenAggregatorAndFingerprintUsed() {
            ServerWebExchange exch = exchange();
            when(aggregator.aggregate(exch.getRequest())).thenReturn(ctx());
            when(fingerprint.fingerprint(eq("slugX"), any())).thenReturn("fp5");
            when(cache.get("fp5")).thenReturn(null);
            when(ruleEngine.resolve(any())).thenReturn(Mono.just(new Resolution("u", 1, "R")));

            StepVerifier.create(service.resolve("slugX", exch))
                    .expectNextMatches(d ->
                            d.targetUrl().equals("u") &&
                                    d.ttlSeconds() == 1 && !d.preserveMethod() &&
                                    d.reason().equals("R"))
                    .verifyComplete();

            verify(aggregator).aggregate(exch.getRequest());
            verify(fingerprint).fingerprint(eq("slugX"), any(RequestContext.class));
            verify(cache).put("fp5", "u", 1);
        }
    }
}
