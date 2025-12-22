package org.millerM907.rules.dsl.operations.impl;

import org.millerM907.rules.dsl.operations.base.Operator;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
public class BetweenTimeOperator implements Operator {
    public String name() {
        return "between";
    }

    public boolean test(Object actual, Object expected) {
        if (actual == null || expected == null) return false;
        String val = String.valueOf(actual);
        String[] hhmm = String.valueOf(expected).split("-");
        LocalTime t = LocalTime.parse(val);
        LocalTime from = LocalTime.parse(hhmm[0]);
        LocalTime to = LocalTime.parse(hhmm[1]);
        return !t.isBefore(from) && !t.isAfter(to);
    }
}
