package org.millerM907.edge.core.fingerprint;

import org.millerM907.edge.models.RequestContext;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.TreeMap;

@Component
public class SimpleFingerprintStrategy implements FingerprintStrategy {

    @Override
    public String fingerprint(String slug, RequestContext ctx) {
        Map<String, String> sorted = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        if (ctx != null && ctx.attrs() != null) {
            ctx.attrs().forEach((k, v) -> {
                if (k != null && v != null && !v.isBlank()) {
                    sorted.put(k, v);
                }
            });
        }

        StringBuilder sb = new StringBuilder(slug == null ? "" : slug);
        sorted.forEach((k, v) -> sb.append('|').append(k).append('=').append(v));

        return sb.toString();
    }
}
