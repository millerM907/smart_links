package org.millerM907.rules.dsl.operations.impl;

import org.millerM907.rules.dsl.operations.base.Operator;
import org.springframework.stereotype.Component;

@Component
public class RegexOperator implements Operator {
    public String name() {
        return "regex";
    }

    public boolean test(Object actual, Object expected) {
        if (actual == null || expected == null) return false;
        return String.valueOf(actual).matches(String.valueOf(expected));
    }
}
