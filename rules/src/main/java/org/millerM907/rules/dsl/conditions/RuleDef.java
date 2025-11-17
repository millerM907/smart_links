package org.millerM907.rules.dsl.conditions;

public record RuleDef(String id, Integer priority, CondDef when, ThenDef then) {
}
