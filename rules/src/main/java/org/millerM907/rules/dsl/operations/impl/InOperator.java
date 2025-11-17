package org.millerM907.rules.dsl.operations.impl;

import org.millerM907.rules.dsl.operations.base.Operator;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Objects;

@Component
public class InOperator implements Operator {
    public String name() {
        return "in";
    }

    public boolean test(Object actual, Object expected) {
        if (expected instanceof Collection<?> c) {
            for (Object v : c) if (Objects.equals(String.valueOf(actual), String.valueOf(v))) return true;
        }
        return false;
    }
}
