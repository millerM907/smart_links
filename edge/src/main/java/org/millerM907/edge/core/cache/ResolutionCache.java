package org.millerM907.edge.core.cache;

public interface ResolutionCache {
    String get(String key);
    void put(String key, String url, int ttlSeconds);
}
