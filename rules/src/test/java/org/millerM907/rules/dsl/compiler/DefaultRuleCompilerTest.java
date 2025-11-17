package org.millerM907.rules.dsl.compiler;

import org.millerM907.rules.dsl.compiler.base.Accessor;
import org.millerM907.rules.dsl.compiler.impl.CompiledRule;
import org.millerM907.rules.dsl.compiler.impl.CompiledRuleSet;
import org.millerM907.rules.dsl.compiler.impl.DefaultRuleCompiler;
import org.millerM907.rules.dsl.conditions.*;
import org.millerM907.rules.dsl.operations.base.Operator;
import org.millerM907.rules.dsl.operations.base.OperatorRegistry;
import org.millerM907.rules.models.Resolution;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("DefaultRuleCompiler — unit tests")
class DefaultRuleCompilerTest {

    private final OperatorRegistry ops = mock(OperatorRegistry.class);
    private final Accessor accessor = mock(Accessor.class);

    private DefaultRuleCompiler compiler() {
        return new DefaultRuleCompiler(ops, accessor);
    }

    private static RuleSet ruleSet(String slug, List<RuleDef> rules, String defUrl) {
        return new RuleSet(slug, defUrl, rules);
    }

    private static RuleDef rule(String id, Integer pr, CondDef when, ThenDef then) {
        return new RuleDef(id, pr, when, then);
    }

    private static ThenDef then(String url, Integer ttl, String reason) {
        return new ThenDef(url, ttl, reason);
    }

    private static PredDef pred(String path, String op, Object val) {
        return new PredDef(path, op, val);
    }

    private static Operator eqOp() {
        return new Operator() {
            @Override
            public String name() {
                return "eq";
            }

            @Override
            public boolean test(Object actual, Object expected) {
                return String.valueOf(actual).equals(String.valueOf(expected));
            }
        };
    }

    @Nested
    @DisplayName("Given rule priorities")
    class GivenPriorities {

        @Test
        @DisplayName("When compile is called then rules are sorted by priority desc")
        void givenPriorities_whenCompile_thenSortedDesc() {
            when(ops.get(anyString())).thenReturn(eqOp());
            when(accessor.read(anyMap(), anyString())).thenAnswer(inv ->
                    ((Map<?, ?>) inv.getArgument(0)).get(inv.getArgument(1)));

            RuleDef low = rule("LOW", 10, new AllDef(List.of(pred("k", "eq", "v"))), then("http://low", 10, "L"));
            RuleDef high = rule("HIGH", 20, new AllDef(List.of(pred("k", "eq", "v"))), then("http://high", 20, "H"));
            RuleSet set = ruleSet("s", List.of(low, high), "http://def");


            CompiledRuleSet crs = compiler().compile("s", set);


            assertThat(crs.commands()).hasSize(2);
            assertThat(crs.commands().get(0).then().targetUrl()).isEqualTo("http://high");
            assertThat(crs.commands().get(1).then().targetUrl()).isEqualTo("http://low");
        }
    }

    @Nested
    @DisplayName("Given logical compositions")
    class GivenLogicalCompositions {

        @Test
        @DisplayName("When 'all' is compiled then all child conditions must match")
        void givenAll_whenCompiled_thenAllMustMatch() {
            when(ops.get("eq")).thenReturn(eqOp());
            when(accessor.read(anyMap(), anyString())).thenAnswer(inv ->
                    ((Map<?, ?>) inv.getArgument(0)).get(inv.getArgument(1)));

            AllDef all = new AllDef(List.of(
                    pred("a", "eq", "X"),
                    pred("b", "eq", "Y")
            ));
            RuleDef r = rule("R", 1, all, then("http://t", 1, "R"));


            CompiledRule rule = compiler().compile("s", ruleSet("s", List.of(r), "d")).commands().get(0);


            Assertions.assertThat(rule.condition().test(Map.of("a", "X", "b", "Y"))).isTrue();
            Assertions.assertThat(rule.condition().test(Map.of("a", "X", "b", "Z"))).isFalse();
        }

        @Test
        @DisplayName("When 'any' is compiled then at least one child must match")
        void givenAny_whenCompiled_thenAnyMatch() {
            when(ops.get("eq")).thenReturn(eqOp());
            when(accessor.read(anyMap(), anyString())).thenAnswer(inv ->
                    ((Map<?, ?>) inv.getArgument(0)).get(inv.getArgument(1)));

            AnyDef any = new AnyDef(List.of(
                    pred("a", "eq", "X"),
                    pred("b", "eq", "Y")
            ));
            RuleDef r = rule("R", 1, any, then("http://t", 1, "R"));


            CompiledRule rule = compiler().compile("s", ruleSet("s", List.of(r), "d")).commands().get(0);


            Assertions.assertThat(rule.condition().test(Map.of("a", "X", "b", "Z"))).isTrue();
            Assertions.assertThat(rule.condition().test(Map.of("a", "Q", "b", "Y"))).isTrue();
            Assertions.assertThat(rule.condition().test(Map.of("a", "Q", "b", "Z"))).isFalse();
        }

        @Test
        @DisplayName("When 'not' is compiled then the inner result is negated")
        void givenNot_whenCompiled_thenNegated() {
            when(ops.get("eq")).thenReturn(eqOp());
            when(accessor.read(anyMap(), anyString())).thenAnswer(inv ->
                    ((Map<?, ?>) inv.getArgument(0)).get(inv.getArgument(1)));

            NotDef not = new NotDef(pred("a", "eq", "X"));
            RuleDef r = rule("R", 1, not, then("http://t", 1, "R"));


            CompiledRule rule = compiler().compile("s", ruleSet("s", List.of(r), "d")).commands().get(0);


            Assertions.assertThat(rule.condition().test(Map.of("a", "X"))).isFalse();
            Assertions.assertThat(rule.condition().test(Map.of("a", "Y"))).isTrue();
        }
    }

    @Nested
    @DisplayName("Given predicate conditions")
    class GivenPredicates {

        @Test
        @DisplayName("When predicate is compiled then OperatorRegistry and Accessor are used")
        void givenPredicate_whenCompiled_thenRegistryAndAccessorUsed() {
            Operator op = spy(eqOp());
            when(ops.get("eq")).thenReturn(op);
            when(accessor.read(anyMap(), eq("browser"))).thenAnswer(inv ->
                    ((Map<?, ?>) inv.getArgument(0)).get("browser"));

            PredDef p = pred("browser", "eq", "Chrome");
            RuleDef r = rule("R", 1, p, then("http://t", 1, "R"));


            CompiledRule rule = compiler().compile("s", ruleSet("s", List.of(r), "d")).commands().get(0);


            Map<String, Object> ctx = Map.of("browser", "Chrome");
            boolean res = rule.condition().test(ctx);

            assertThat(res).isTrue();
            verify(ops, times(1)).get("eq");
            verify(accessor, times(1)).read(ctx, "browser");
        }

        @Test
        @DisplayName("When ttl and reason are null then defaults are applied in Resolution")
        void givenNullTtlReason_whenCompile_thenDefaultsApplied() {
            when(ops.get(anyString())).thenReturn(eqOp());
            when(accessor.read(anyMap(), anyString())).thenReturn("v");

            RuleDef r = rule("R", 1, pred("k", "eq", "v"), then("http://x", null, null));


            CompiledRuleSet crs = compiler().compile("s", ruleSet("s", List.of(r), "d"));
            Resolution res = crs.commands().get(0).then();


            assertThat(res.ttlSeconds()).isEqualTo(60);
            assertThat(res.reason()).isEqualTo("RULE_MATCH");
            assertThat(res.targetUrl()).isEqualTo("http://x");
        }
    }

    @Nested
    @DisplayName("Given edge cases")
    class GivenEdgeCases {

        @Test
        @DisplayName("When RuleSet has no rules then compiled set has empty commands and keeps defaultUrl")
        void givenNoRules_whenCompile_thenEmptyCommandsAndDefaultUrlKept() {
            CompiledRuleSet crs = compiler().compile("s", ruleSet("s", List.of(), "http://def"));
            assertThat(crs.commands()).isEmpty();
            assertThat(crs.defaultUrl()).isEqualTo("http://def");
        }

        @Test
        @DisplayName("When 'when' is null then a NullPointerException is thrown during compilation")
        void givenNullWhen_whenCompile_thenNpe() {
            RuleDef bad = rule("BAD", 1, null, then("u", 1, "R"));
            RuleSet set = ruleSet("s", List.of(bad), "d");
            assertThatThrownBy(() -> compiler().compile("s", set))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
