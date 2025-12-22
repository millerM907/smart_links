package org.millerM907.rules.dsl.compiler.impl;

import org.millerM907.rules.dsl.compiler.base.Condition;
import org.millerM907.rules.models.Resolution;

public record CompiledRule(Condition condition, Resolution then) {
}
