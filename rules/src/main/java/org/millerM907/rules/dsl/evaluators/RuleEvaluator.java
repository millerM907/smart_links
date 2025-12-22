package org.millerM907.rules.dsl.evaluators;

import org.millerM907.rules.dsl.compiler.impl.CompiledRuleSet;
import org.millerM907.rules.models.Resolution;
import org.millerM907.rules.models.ResolveRequest;

public interface RuleEvaluator {
    Resolution evaluate(ResolveRequest req, CompiledRuleSet crs);
}
