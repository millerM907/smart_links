package org.millerM907.edge.services;

import org.millerM907.edge.models.RedirectDecision;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public interface RedirectService {
    Mono<RedirectDecision> resolve(String slug, ServerWebExchange exchange);
}
