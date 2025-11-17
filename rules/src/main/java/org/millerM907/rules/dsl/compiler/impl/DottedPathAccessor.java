package org.millerM907.rules.dsl.compiler.impl;

import org.millerM907.rules.dsl.compiler.base.Accessor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DottedPathAccessor implements Accessor {
    @SuppressWarnings("unchecked")
    public Object read(Map<String, Object> ctx, String path) {
        Object cur = ctx;
        for (String p : path.split("\\.")) {
            if (!(cur instanceof Map<?, ?> m)) return null;
            cur = m.get(p);
            if (cur == null) return null;
        }
        return cur;
    }
}
