package org.millerM907.rules.controllers;

import org.millerM907.rules.services.ResolveUseCase;
import org.millerM907.rules.models.Resolution;
import org.millerM907.rules.models.ResolveRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ResolveController {
    private final ResolveUseCase useCase;

    public ResolveController(ResolveUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/resolve")
    public ResponseEntity<Resolution> resolve(@RequestBody ResolveRequest request) {
        return ResponseEntity.ok(useCase.resolve(request));
    }
}
