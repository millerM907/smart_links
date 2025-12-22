package org.millerM907.edge.core.context_aggregator.extractors.impl;

import org.millerM907.edge.core.context_aggregator.extractors.base.AttributeExtractor;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import java.util.LinkedHashMap;
import java.util.Map;

@Order(6)
@Component
public class AttrsPrefixedQueryExtractor implements AttributeExtractor {

    @Override
    public Map<String, String> extract(ServerHttpRequest request) {
        Map<String, String> out = new LinkedHashMap<>();
        MultiValueMap<String, String> qp = request.getQueryParams();

        qp.forEach((k, values) -> {
            if (k.startsWith("attrs.")) {
                String first = qp.getFirst(k);
                if (first != null) {
                    out.put(k.substring(6), first);
                }
            }
        });
        return out;
    }
}
