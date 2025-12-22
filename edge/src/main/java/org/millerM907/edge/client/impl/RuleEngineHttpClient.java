package org.millerM907.edge.client.impl;

import org.millerM907.edge.models.Resolution;
import org.millerM907.edge.models.ResolveRequest;
import org.millerM907.edge.client.base.RuleEnginePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class RuleEngineHttpClient implements RuleEnginePort {

    private final WebClient client;

    public RuleEngineHttpClient(@Value("${rules.baseUrl:http://localhost:8081}") String baseUrl) {
        this.client = WebClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public Mono<Resolution> resolve(ResolveRequest req) {
        return client.post()
                .uri("/resolve")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .retrieve()
                .bodyToMono(Resolution.class);
    }
}
