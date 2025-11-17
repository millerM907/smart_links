package org.millerM907.rules.dsl.compiler.impl;

import org.millerM907.rules.dsl.compiler.base.Accessor;
import org.millerM907.rules.dsl.compiler.base.Condition;
import org.millerM907.rules.dsl.compiler.base.RuleCompiler;
import org.millerM907.rules.dsl.conditions.*;
import org.millerM907.rules.dsl.operations.base.OperatorRegistry;
import org.millerM907.rules.models.Resolution;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
public class DefaultRuleCompiler implements RuleCompiler {
    private final OperatorRegistry operatorRegistry;
    private final Accessor accessor;

    public DefaultRuleCompiler(OperatorRegistry operatorRegistry, Accessor accessor) {
        this.operatorRegistry = operatorRegistry;
        this.accessor = accessor;
    }

    @Override
    public CompiledRuleSet compile(String slug, RuleSet set) {
        List<CompiledRule> compiled = set.rules().stream()
                .sorted(Comparator.comparingInt(
                        (RuleDef r) -> Optional.ofNullable(r.priority()).orElse(0)
                ).reversed())
                .map(r -> new CompiledRule(cond(r.when()), toResolution(r.then())))
                .toList();
        return new CompiledRuleSet(compiled, set.defaultUrl());
    }

    private Condition cond(CondDef d) {
        if (d instanceof AllDef a) {
            var cs = a.all().stream().map(this::cond).toList();
            return ctx -> cs.stream().allMatch(c -> c.test(ctx));
        } else if (d instanceof AnyDef a) {
            var cs = a.any().stream().map(this::cond).toList();
            return ctx -> cs.stream().anyMatch(c -> c.test(ctx));
        } else if (d instanceof NotDef n) {
            var inner = cond(n.not());
            return ctx -> !inner.test(ctx);
        } else if (d instanceof PredDef p) {
            var op = operatorRegistry.get(p.operator());
            return ctx -> op.test(accessor.read(ctx, p.path()), p.value());
        } else {
            throw new IllegalArgumentException("Unsupported CondDef type: " + d.getClass());
        }
    }

    private Resolution toResolution(ThenDef t) {
        int ttl = t.ttl() != null ? t.ttl() : 60;
        String reason = t.reason() != null ? t.reason() : "RULE_MATCH";
        return new Resolution(t.url(), ttl, reason);
    }
}
