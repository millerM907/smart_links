package org.millerM907.rules.dsl.compiler.impl;

import java.util.List;

public record CompiledRuleSet(List<CompiledRule> commands, String defaultUrl) {
}
