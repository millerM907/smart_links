package org.millerM907.edge.core.context_aggregator.extractors.impl;

import org.millerM907.edge.core.context_aggregator.extractors.base.AttributeExtractor;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.Map;

@Order(7)
@Component
public class LanguageExtractor implements AttributeExtractor {

    @Override
    public Map<String, String> extract(ServerHttpRequest request) {
        String header = request.getHeaders().getFirst("Accept-Language");
        String lang = resolvePrimaryLanguage(header);
        return Map.of("lang", lang);
    }

    private String resolvePrimaryLanguage(String header) {
        if (header == null || header.isBlank()) {
            return "UNKNOWN";
        }

        String firstPart = header.split(",")[0].trim();
        if (firstPart.isEmpty()) {
            return "UNKNOWN";
        }

        int semicolonIndex = firstPart.indexOf(';');
        String code = (semicolonIndex >= 0 ? firstPart.substring(0, semicolonIndex) : firstPart).trim();

        return code.isEmpty() ? "UNKNOWN" : code;
    }
}
