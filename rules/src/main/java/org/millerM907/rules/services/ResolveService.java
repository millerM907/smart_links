package org.millerM907.rules.services;

import org.millerM907.rules.dsl.compiler.base.RuleCompiler;
import org.millerM907.rules.dsl.evaluators.RuleEvaluator;
import org.millerM907.rules.repository.base.RuleSetRepository;
import org.millerM907.rules.dsl.compiler.impl.CompiledRuleSet;
import org.millerM907.rules.models.Resolution;
import org.millerM907.rules.models.ResolveRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ResolveService implements ResolveUseCase {
    private final RuleSetRepository repo;
    private final RuleCompiler compiler;
    private final RuleEvaluator evaluator;
    private final String fallbackUrl;

    private static final class Entry {
        final long ver;
        final CompiledRuleSet crs;

        Entry(long v, CompiledRuleSet c) {
            ver = v;
            crs = c;
        }
    }

    private final ConcurrentHashMap<String, Entry> cache = new ConcurrentHashMap<>();

    public ResolveService(RuleSetRepository repo,
                          RuleCompiler compiler,
                          RuleEvaluator evaluator,
                          @Value("${rules.fallbackUrl:http://localhost:8082/landing/default}") String fallbackUrl) {
        this.repo = repo;
        this.compiler = compiler;
        this.evaluator = evaluator;
        this.fallbackUrl = fallbackUrl;
    }

    @Override
    public Resolution resolve(ResolveRequest request) {
        long currentVersion = repo.version();

        Entry entry = cache.compute(request.slug(), (slug, previous) -> {
            if (previous != null && previous.ver == currentVersion) {
                return previous;
            }

            try {
                CompiledRuleSet compiled = compiler.compile(slug, repo.current(slug));
                return new Entry(currentVersion, compiled);
            } catch (IllegalArgumentException unknownSlug) {
                CompiledRuleSet fallbackRuleSet =
                        new CompiledRuleSet(List.of(), fallbackUrl);
                return new Entry(currentVersion, fallbackRuleSet);
            }
        });

        return evaluator.evaluate(request, entry.crs);
    }
}
