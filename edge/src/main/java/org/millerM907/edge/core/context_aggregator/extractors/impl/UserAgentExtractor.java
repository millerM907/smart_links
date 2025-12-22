package org.millerM907.edge.core.context_aggregator.extractors.impl;

import org.millerM907.edge.core.context_aggregator.extractors.base.AttributeExtractor;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.Map;

@Order(0)
@Component
public class UserAgentExtractor implements AttributeExtractor {

    @Override
    public Map<String, String> extract(ServerHttpRequest request) {
        String userAgent = request.getHeaders().getFirst("User-Agent");
        if (userAgent == null || userAgent.isBlank()) {
            return Map.of();
        }
        return Map.of("userAgent", userAgent);
    }
}
