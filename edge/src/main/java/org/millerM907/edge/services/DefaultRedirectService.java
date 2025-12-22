package org.millerM907.edge.services;

import org.millerM907.edge.client.base.RuleEnginePort;
import org.millerM907.edge.core.cache.ResolutionCache;
import org.millerM907.edge.models.RedirectDecision;
import org.millerM907.edge.models.RequestContext;
import org.millerM907.edge.models.ResolveRequest;
import org.millerM907.edge.core.fingerprint.FingerprintStrategy;
import org.millerM907.edge.core.context_aggregator.RequestContextAggregator;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Service
public class DefaultRedirectService implements RedirectService {

    private final FingerprintStrategy fingerprint;
    private final ResolutionCache cache;
    private final RuleEnginePort ruleEngine;
    private final RequestContextAggregator contextAggregator;

    public DefaultRedirectService(FingerprintStrategy fingerprint,
                                  ResolutionCache cache,
                                  RuleEnginePort ruleEngine,
                                  RequestContextAggregator contextAggregator) {
        this.fingerprint = fingerprint;
        this.cache = cache;
        this.ruleEngine = ruleEngine;
        this.contextAggregator = contextAggregator;
    }


    @Override
    public Mono<RedirectDecision> resolve(String slug, ServerWebExchange exchange) {
        RequestContext context = contextAggregator.aggregate(exchange.getRequest());

        String fp = fingerprint.fingerprint(slug, context);

        String cachedUrl = cache.get(fp);
        if (cachedUrl != null) {
            return Mono.just(new RedirectDecision(cachedUrl, false, 0, "CACHE_HIT"));
        }

        return ruleEngine.resolve(new ResolveRequest(slug, context))
                .map(res -> {
                    cache.put(fp, res.targetUrl(), res.ttlSeconds());
                    return new RedirectDecision(res.targetUrl(), false, res.ttlSeconds(), res.reason());
                });
    }
}
