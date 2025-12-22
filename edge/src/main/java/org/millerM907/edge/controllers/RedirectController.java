package org.millerM907.edge.controllers;

import org.millerM907.edge.services.RedirectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Controller
public class RedirectController {

    private final RedirectService redirectService;

    public RedirectController(RedirectService redirectService) {
        this.redirectService = redirectService;
    }

    @GetMapping("/s/{slug}")
    public Mono<ResponseEntity<Void>> redirect(@PathVariable("slug") String slug,
                                               ServerWebExchange exchange) {
        return redirectService.resolve(slug, exchange)
                .map(d -> {
                    HttpStatus code = d.preserveMethod()
                            ? HttpStatus.TEMPORARY_REDIRECT   // 307
                            : HttpStatus.FOUND;               // 302
                    return ResponseEntity.status(code)
                            .header("Location", d.targetUrl())
                            .build();
                });
    }
}
