package org.millerM907.rules.dsl.compiler.base;

import org.millerM907.rules.dsl.conditions.RuleSet;
import org.millerM907.rules.dsl.compiler.impl.CompiledRuleSet;

public interface RuleCompiler {
    CompiledRuleSet compile(String slug, RuleSet set);
}
