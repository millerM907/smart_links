package org.millerM907.rules.services;

import org.millerM907.rules.models.Resolution;
import org.millerM907.rules.models.ResolveRequest;

public interface ResolveUseCase {
    Resolution resolve(ResolveRequest req);
}
