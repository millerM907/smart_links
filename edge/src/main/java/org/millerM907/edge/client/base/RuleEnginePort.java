package org.millerM907.edge.client.base;

import org.millerM907.edge.models.Resolution;
import org.millerM907.edge.models.ResolveRequest;
import reactor.core.publisher.Mono;

public interface RuleEnginePort {
    Mono<Resolution> resolve(ResolveRequest req);
}
