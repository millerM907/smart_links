package org.millerM907.rules.dsl.compiler.base;

import java.util.Map;

public interface Condition {
    boolean test(Map<String, Object> ctx);
}
