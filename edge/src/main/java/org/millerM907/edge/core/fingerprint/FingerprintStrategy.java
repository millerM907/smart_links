package org.millerM907.edge.core.fingerprint;

import org.millerM907.edge.models.RequestContext;

public interface FingerprintStrategy {
    String fingerprint(String slug, RequestContext ctx);
}
