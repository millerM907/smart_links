package org.millerM907.edge.core.support.impl;

import org.millerM907.edge.core.support.base.TimeProvider;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
public class SystemTimeProvider implements TimeProvider {
    @Override
    public LocalTime now() {
        return LocalTime.now();
    }
}
