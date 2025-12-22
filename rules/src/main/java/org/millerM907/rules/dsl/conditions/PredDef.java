package org.millerM907.rules.dsl.conditions;

public record PredDef(String path, String operator, Object value) implements CondDef {
}
