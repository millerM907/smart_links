package org.millerM907.rules.dsl.conditions;

import java.util.List;

public record RuleSet(String slug, String defaultUrl, List<RuleDef> rules) {
}
