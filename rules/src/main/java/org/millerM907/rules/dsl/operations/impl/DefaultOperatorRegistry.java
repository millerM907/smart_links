package org.millerM907.rules.dsl.operations.impl;

import org.millerM907.rules.dsl.operations.base.Operator;
import org.millerM907.rules.dsl.operations.base.OperatorRegistry;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class DefaultOperatorRegistry implements OperatorRegistry {
    private final Map<String, Operator> byName;

    public DefaultOperatorRegistry(List<Operator> ops) {
        Map<String, Operator> tmp = new LinkedHashMap<>();
        if (ops != null) {
            for (Operator o : ops) {
                tmp.putIfAbsent(o.name(), o);
            }
        }
        this.byName = Collections.unmodifiableMap(tmp);
    }

    @Override
    public Operator get(String name) {
        Operator op = byName.get(name);
        if (op == null) throw new IllegalArgumentException("Unknown operator: " + name);
        return op;
    }
}
