package org.millerM907.rules.dsl.evaluators;

import org.millerM907.rules.dsl.compiler.impl.CompiledRule;
import org.millerM907.rules.dsl.compiler.impl.CompiledRuleSet;
import org.millerM907.rules.models.Resolution;
import org.millerM907.rules.models.ResolveRequest;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class DefaultRuleEvaluator implements RuleEvaluator {
    @Override
    public Resolution evaluate(ResolveRequest req, CompiledRuleSet crs) {
        Map<String, Object> ctx = new LinkedHashMap<>(req.context().attrs());
        for (CompiledRule rule : crs.commands()) {
            if (rule.condition().test(ctx)) return rule.then();
        }
        String url = (crs.defaultUrl() != null && !crs.defaultUrl().isBlank())
                ? crs.defaultUrl() : "http://localhost:8082/landing/default";
        return new Resolution(url, 60, "DEFAULT");
    }
}
