package org.millerM907.rules.dsl.evaluators;

import org.millerM907.rules.dsl.compiler.base.Condition;
import org.millerM907.rules.dsl.compiler.impl.CompiledRule;
import org.millerM907.rules.dsl.compiler.impl.CompiledRuleSet;
import org.millerM907.rules.models.RequestContext;
import org.millerM907.rules.models.Resolution;
import org.millerM907.rules.models.ResolveRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DefaultRuleEvaluator — unit tests")
class DefaultRuleEvaluatorTest {

    private final DefaultRuleEvaluator evaluator = new DefaultRuleEvaluator();

    private static ResolveRequest req() {
        return new ResolveRequest("slug", new RequestContext(Map.of("browser", "Chrome", "time", "10:00")));
    }

    private static Condition t() { return ctx -> true; }
    private static Condition f() { return ctx -> false; }

    @Nested
    @DisplayName("Given multiple rules with matches")
    class GivenMultipleMatches {

        @Test
        @DisplayName("When the first rule matches then it is returned")
        void givenFirstMatches_whenEvaluate_thenFirstReturned() {
            Resolution r1 = new Resolution("http://a", 11, "A");
            Resolution r2 = new Resolution("http://b", 22, "B");
            CompiledRuleSet crs = new CompiledRuleSet(
                    List.of(new CompiledRule(t(), r1), new CompiledRule(t(), r2)),
                    "http://default"
            );


            Resolution out = evaluator.evaluate(req(), crs);


            assertThat(out.targetUrl()).isEqualTo("http://a");
            assertThat(out.ttlSeconds()).isEqualTo(11);
            assertThat(out.reason()).isEqualTo("A");
        }

        @Test
        @DisplayName("When the first rule is false and the second is true then the second is returned")
        void givenFirstFalseSecondTrue_whenEvaluate_thenSecondReturned() {
            Resolution r1 = new Resolution("http://a", 11, "A");
            Resolution r2 = new Resolution("http://b", 22, "B");
            CompiledRuleSet crs = new CompiledRuleSet(
                    List.of(new CompiledRule(f(), r1), new CompiledRule(t(), r2)),
                    "http://default"
            );


            Resolution out = evaluator.evaluate(req(), crs);


            assertThat(out.targetUrl()).isEqualTo("http://b");
            assertThat(out.ttlSeconds()).isEqualTo(22);
            assertThat(out.reason()).isEqualTo("B");
        }
    }

    @Nested
    @DisplayName("Given no rule matches")
    class GivenNoMatch {

        @Test
        @DisplayName("When defaultUrl is provided then it is returned with DEFAULT reason")
        void givenNoMatch_whenDefaultUrlProvided_thenDefaultReturned() {
            CompiledRuleSet crs = new CompiledRuleSet(
                    List.of(new CompiledRule(f(), new Resolution("http://a", 11, "A"))),
                    "http://default-url"
            );


            Resolution out = evaluator.evaluate(req(), crs);


            assertThat(out.targetUrl()).isEqualTo("http://default-url");
            assertThat(out.ttlSeconds()).isEqualTo(60);
            assertThat(out.reason()).isEqualTo("DEFAULT");
        }

        @Test
        @DisplayName("When defaultUrl is null then global fallback is returned")
        void givenNoMatch_whenDefaultUrlNull_thenGlobalFallbackReturned() {
            CompiledRuleSet crs = new CompiledRuleSet(
                    List.of(new CompiledRule(f(), new Resolution("http://a", 11, "A"))),
                    null
            );


            Resolution out = evaluator.evaluate(req(), crs);


            assertThat(out.targetUrl()).isEqualTo("http://localhost:8082/landing/default");
            assertThat(out.ttlSeconds()).isEqualTo(60);
            assertThat(out.reason()).isEqualTo("DEFAULT");
        }

        @Test
        @DisplayName("When defaultUrl is blank then global fallback is returned")
        void givenNoMatch_whenDefaultUrlBlank_thenGlobalFallbackReturned() {
            CompiledRuleSet crs = new CompiledRuleSet(
                    List.of(new CompiledRule(f(), new Resolution("http://a", 11, "A"))),
                    "   "
            );


            Resolution out = evaluator.evaluate(req(), crs);


            assertThat(out.targetUrl()).isEqualTo("http://localhost:8082/landing/default");
            assertThat(out.ttlSeconds()).isEqualTo(60);
            assertThat(out.reason()).isEqualTo("DEFAULT");
        }
    }

    @Nested
    @DisplayName("Given empty rule set")
    class GivenEmptyRules {

        @Test
        @DisplayName("When there are no commands then defaultUrl is used if present")
        void givenEmpty_whenDefaultPresent_thenDefaultUsed() {
            CompiledRuleSet crs = new CompiledRuleSet(List.of(), "http://default-here");


            Resolution out = evaluator.evaluate(req(), crs);


            assertThat(out.targetUrl()).isEqualTo("http://default-here");
            assertThat(out.ttlSeconds()).isEqualTo(60);
            assertThat(out.reason()).isEqualTo("DEFAULT");
        }

        @Test
        @DisplayName("When there are no commands and no defaultUrl then global fallback is used")
        void givenEmpty_whenNoDefault_thenGlobalFallbackUsed() {
            CompiledRuleSet crs = new CompiledRuleSet(List.of(), null);


            Resolution out = evaluator.evaluate(req(), crs);


            assertThat(out.targetUrl()).isEqualTo("http://localhost:8082/landing/default");
            assertThat(out.ttlSeconds()).isEqualTo(60);
            assertThat(out.reason()).isEqualTo("DEFAULT");
        }
    }
}
