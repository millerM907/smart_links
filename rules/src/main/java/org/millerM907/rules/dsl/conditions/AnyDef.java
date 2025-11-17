package org.millerM907.rules.dsl.conditions;

import java.util.List;

public record AnyDef(List<CondDef> any) implements CondDef {
}
