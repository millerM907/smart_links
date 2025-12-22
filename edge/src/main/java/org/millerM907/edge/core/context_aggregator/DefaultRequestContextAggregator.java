package org.millerM907.edge.core.context_aggregator;

import org.millerM907.edge.core.context_aggregator.extractors.base.AttributeExtractor;
import org.millerM907.edge.models.RequestContext;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class DefaultRequestContextAggregator implements RequestContextAggregator {

    private final List<AttributeExtractor> extractors;

    public DefaultRequestContextAggregator(List<AttributeExtractor> extractors) {
        this.extractors = extractors;
    }

    @Override
    public RequestContext aggregate(ServerHttpRequest request) {
        Map<String, String> attrs = new LinkedHashMap<>();

        MultiValueMap<String, String> qp = request.getQueryParams();
        qp.forEach((k, values) -> {
            String v = qp.getFirst(k);
            if (v != null) attrs.put(k, v);
        });

        for (AttributeExtractor ex : extractors) {
            if (ex.supports(request)) {
                attrs.putAll(ex.extract(request));
            }
        }
        return new RequestContext(attrs);
    }
}
