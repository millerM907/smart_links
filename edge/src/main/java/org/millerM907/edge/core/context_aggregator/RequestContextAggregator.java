package org.millerM907.edge.core.context_aggregator;

import org.millerM907.edge.models.RequestContext;
import org.springframework.http.server.reactive.ServerHttpRequest;

public interface RequestContextAggregator {
    RequestContext aggregate(ServerHttpRequest request);
}
