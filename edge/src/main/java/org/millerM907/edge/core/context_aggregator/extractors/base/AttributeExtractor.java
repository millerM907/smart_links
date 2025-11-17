package org.millerM907.edge.core.context_aggregator.extractors.base;

import org.springframework.http.server.reactive.ServerHttpRequest;

import java.util.Map;

public interface AttributeExtractor {
    /** Можно всегда вернуть true, либо фильтровать по условиям/заголовкам/пути и т.д. */
    default boolean supports(ServerHttpRequest request) { return true; }

    /** Вернуть 0..N пар (key=value), которые попадут в контекст. */
    Map<String, String> extract(ServerHttpRequest request);
}
