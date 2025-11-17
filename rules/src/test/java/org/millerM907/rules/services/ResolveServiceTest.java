package org.millerM907.rules.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.millerM907.rules.dsl.compiler.base.RuleCompiler;
import org.millerM907.rules.dsl.compiler.impl.CompiledRuleSet;
import org.millerM907.rules.dsl.conditions.RuleSet;
import org.millerM907.rules.dsl.evaluators.RuleEvaluator;
import org.millerM907.rules.models.RequestContext;
import org.millerM907.rules.models.Resolution;
import org.millerM907.rules.models.ResolveRequest;
import org.millerM907.rules.repository.base.RuleSetRepository;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("ResolveService — unit tests")
class ResolveServiceTest {

    private final RuleSetRepository repo = mock(RuleSetRepository.class);
    private final RuleCompiler compiler = mock(RuleCompiler.class);
    private final RuleEvaluator evaluator = mock(RuleEvaluator.class);

    private final String fallbackUrl = "http://fallback";
    private final ResolveService service = new ResolveService(repo, compiler, evaluator, fallbackUrl);

    private static ResolveRequest req(String slug) {
        return new ResolveRequest(slug, new RequestContext(Map.of("browser", "Chrome")));
    }

    private static CompiledRuleSet crs(String marker) {
        return new CompiledRuleSet(List.of(), "default-" + marker);
    }

    private static RuleSet ruleSet(String slug) {
        // любой минимальный валидный RuleSet
        return new RuleSet(slug, "http://default-" + slug, List.of());
    }

    @Nested
    @DisplayName("Given an empty cache")
    class GivenEmptyCache {

        @Test
        @DisplayName("When resolve is called then it compiles, caches and returns evaluator result")
        void givenEmptyCache_whenResolve_thenCompilesCachesAndReturns() {
            when(repo.version()).thenReturn(1L);
            RuleSet rs = ruleSet("sale1111");
            when(repo.current("sale1111")).thenReturn(rs);
            when(compiler.compile("sale1111", rs)).thenReturn(crs("v1"));
            Resolution expected = new Resolution("http://t", 60, "R1");
            when(evaluator.evaluate(any(ResolveRequest.class), any(CompiledRuleSet.class)))
                    .thenReturn(expected);

            Resolution out = service.resolve(req("sale1111"));

            assertThat(out).isEqualTo(expected);
            verify(repo, times(1)).version();
            verify(repo, times(1)).current("sale1111");
            verify(compiler, times(1)).compile("sale1111", rs);
            verify(evaluator, times(1)).evaluate(any(ResolveRequest.class), eq(crs("v1")));
        }

        @Test
        @DisplayName("When repository throws for unknown slug then fallback CompiledRuleSet is used")
        void givenUnknownSlug_whenResolve_thenUsesFallbackRuleSet() {
            when(repo.version()).thenReturn(1L);
            when(repo.current("unknown"))
                    .thenThrow(new IllegalArgumentException("Unknown slug: unknown"));

            ArgumentCaptor<CompiledRuleSet> crsCaptor = ArgumentCaptor.forClass(CompiledRuleSet.class);
            Resolution expected = new Resolution(fallbackUrl, 60, "FALLBACK");
            when(evaluator.evaluate(any(ResolveRequest.class), any(CompiledRuleSet.class)))
                    .thenReturn(expected);

            Resolution out = service.resolve(req("unknown"));

            assertThat(out).isEqualTo(expected);

            verify(repo, times(1)).version();
            verify(repo, times(1)).current("unknown");
            verify(compiler, never()).compile(anyString(), any());

            verify(evaluator, times(1)).evaluate(any(ResolveRequest.class), crsCaptor.capture());
            CompiledRuleSet used = crsCaptor.getValue();
            assertThat(used.defaultUrl()).isEqualTo(fallbackUrl);
            assertThat(used.commands()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Given a cached entry with the same repository version")
    class GivenCacheSameVersion {

        @Test
        @DisplayName("When resolve is called twice then the second call uses cache and skips compilation")
        void givenCacheSameVersion_whenResolveTwice_thenSecondSkipsCompile() {
            when(repo.version()).thenReturn(7L);
            RuleSet rs = ruleSet("sale1111");
            when(repo.current("sale1111")).thenReturn(rs);
            when(compiler.compile("sale1111", rs)).thenReturn(crs("v7"));
            when(evaluator.evaluate(any(), any()))
                    .thenReturn(new Resolution("http://v7", 60, "R"));

            Resolution first = service.resolve(req("sale1111"));
            Resolution second = service.resolve(req("sale1111"));

            assertThat(first.targetUrl()).isEqualTo("http://v7");
            assertThat(second.targetUrl()).isEqualTo("http://v7");
            verify(repo, times(2)).version();
            verify(repo, times(1)).current("sale1111");
            verify(compiler, times(1)).compile("sale1111", rs);
            verify(evaluator, times(2)).evaluate(any(), any());
        }
    }

    @Nested
    @DisplayName("Given a cached entry but repository version changes")
    class GivenVersionChanged {

        @Test
        @DisplayName("When version increases then the cache is invalidated and compilation runs again")
        void givenVersionChange_whenResolve_thenRecompiles() {
            when(repo.version()).thenReturn(1L, 2L);
            RuleSet rs = ruleSet("sale1111");
            when(repo.current("sale1111")).thenReturn(rs);
            when(compiler.compile("sale1111", rs))
                    .thenReturn(crs("v1"))
                    .thenReturn(crs("v2"));
            when(evaluator.evaluate(any(), any()))
                    .thenReturn(new Resolution("http://v1", 60, "R1"))
                    .thenReturn(new Resolution("http://v2", 60, "R2"));

            Resolution r1 = service.resolve(req("sale1111"));
            Resolution r2 = service.resolve(req("sale1111"));

            assertThat(r1.targetUrl()).isEqualTo("http://v1");
            assertThat(r2.targetUrl()).isEqualTo("http://v2");
            verify(repo, times(2)).version();
            verify(repo, times(2)).current("sale1111");
            verify(compiler, times(2)).compile("sale1111", rs);
            verify(evaluator, times(2)).evaluate(any(), any());
        }
    }

    @Nested
    @DisplayName("Given evaluator behavior")
    class GivenEvaluator {

        @Test
        @DisplayName("When evaluator returns different results then resolve returns them verbatim")
        void givenEvaluatorReturns_whenResolve_thenReturnedVerbatim() {
            when(repo.version()).thenReturn(3L, 3L);
            RuleSet rs = ruleSet("sale1111");
            when(repo.current("sale1111")).thenReturn(rs);
            when(compiler.compile("sale1111", rs)).thenReturn(crs("v3"));

            when(evaluator.evaluate(any(), any()))
                    .thenReturn(new Resolution("http://a", 10, "A"))
                    .thenReturn(new Resolution("http://b", 20, "B"));

            Resolution a = service.resolve(req("sale1111"));
            Resolution b = service.resolve(req("sale1111"));

            assertThat(a.targetUrl()).isEqualTo("http://a");
            assertThat(b.targetUrl()).isEqualTo("http://b");
        }
    }
}