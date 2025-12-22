package org.millerM907.rules.repository.base;

import org.millerM907.rules.dsl.conditions.RuleSet;

public interface RuleSetRepository {
    RuleSet current(String slug);

    long version();
}
