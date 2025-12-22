package org.millerM907.rules.dsl.operations.impl;

import org.millerM907.rules.dsl.operations.base.Operator;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class EqOperator implements Operator {
    public String name() {
        return "eq";
    }

    public boolean test(Object actual, Object expected) {
        return Objects.equals(String.valueOf(actual), String.valueOf(expected));
    }
}
