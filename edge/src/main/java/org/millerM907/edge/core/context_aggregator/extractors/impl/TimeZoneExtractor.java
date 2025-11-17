package org.millerM907.edge.core.context_aggregator.extractors.impl;

import org.millerM907.edge.core.context_aggregator.extractors.base.AttributeExtractor;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.Map;

@Order(3)
@Component
public class TimeZoneExtractor implements AttributeExtractor {

    @Override
    public Map<String, String> extract(ServerHttpRequest request) {
        String v = request.getHeaders().getFirst("X-Demo-Tz");
        return Map.of("tz", (v != null && !v.isBlank()) ? v : "UTC");
    }
}
