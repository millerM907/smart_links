package org.millerM907.rules.dsl.compiler.base;

import java.util.Map;

public interface Accessor {
    Object read(Map<String, Object> ctx, String path);
}
