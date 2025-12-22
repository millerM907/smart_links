package org.millerM907.rules.repository.impl;

import org.millerM907.rules.dsl.conditions.DslRoot;
import org.millerM907.rules.dsl.conditions.RuleSet;
import org.millerM907.rules.repository.base.DslLoader;
import org.millerM907.rules.repository.base.RuleSetRepository;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Repository
public class InMemoryRuleSetRepository implements RuleSetRepository {
    private final DslLoader loader;
    private final AtomicReference<Map<String, RuleSet>> index = new AtomicReference<>(Map.of());
    private final AtomicLong version = new AtomicLong(0);

    public InMemoryRuleSetRepository(DslLoader loader) {
        this.loader = loader;
        reload();
    }

    @Override
    public RuleSet current(String slug) {
        RuleSet rs = index.get().get(slug);
        if (rs == null) throw new IllegalArgumentException("Unknown slug: " + slug);
        return rs;
    }

    @Override
    public long version() {
        return version.get();
    }

    public synchronized void reload() {
        DslRoot root = loader.load();
        Map<String, RuleSet> idx = new LinkedHashMap<>();
        if (root != null && root.links() != null) {
            for (RuleSet rs : root.links()) if (rs.slug() != null) idx.put(rs.slug(), rs);
        }
        index.set(Collections.unmodifiableMap(idx));
        version.incrementAndGet();
    }
}
