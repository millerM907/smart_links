package org.millerM907.edge.core.context_aggregator.extractors.impl;

import org.millerM907.edge.core.context_aggregator.extractors.base.AttributeExtractor;
import org.millerM907.edge.core.support.base.TimeProvider;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.Map;

@Order(2)
@Component
public class TimeExtractor implements AttributeExtractor {

    private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");
    private final TimeProvider timeProvider;

    public TimeExtractor(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    @Override
    public Map<String, String> extract(ServerHttpRequest request) {
        String v = request.getHeaders().getFirst("X-Demo-Time");
        if (v != null && !v.isBlank()) return Map.of("time", v);
        return Map.of("time", timeProvider.now().format(HH_MM));
    }
}
