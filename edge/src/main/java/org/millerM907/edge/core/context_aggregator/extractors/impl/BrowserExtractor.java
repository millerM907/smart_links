package org.millerM907.edge.core.context_aggregator.extractors.impl;

import org.millerM907.edge.core.context_aggregator.extractors.base.AttributeExtractor;
import org.millerM907.edge.core.support.base.BrowserDetector;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.Map;

@Order(5)
@Component
public class BrowserExtractor implements AttributeExtractor {

    private final BrowserDetector detector;

    public BrowserExtractor(BrowserDetector detector) {
        this.detector = detector;
    }

    @Override
    public Map<String, String> extract(ServerHttpRequest request) {
        String override = request.getHeaders().getFirst("X-Demo-Browser");
        if (override != null && !override.isBlank()) {
            return Map.of("browser", override);
        }
        String userAgent = request.getHeaders().getFirst("User-Agent");
        String browser = detector.detect(userAgent);
        return Map.of("browser", browser != null ? browser : "Unknown");
    }
}
