package org.millerM907.edge.core.cache;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CaffeineResolutionCache — unit tests")
class CaffeineResolutionCacheTest {

    @Nested
    @DisplayName("Given a missing key")
    class GivenMissingKey {

        @Test
        @DisplayName("When get is called then it returns null")
        void givenMissingKey_whenGet_thenReturnsNull() {
            CaffeineResolutionCache cache = new CaffeineResolutionCache();
            String value = cache.get("missing");
            assertThat(value).isNull();
        }
    }

    @Nested
    @DisplayName("Given an empty cache")
    class GivenEmptyCache {

        @Test
        @DisplayName("When put is called then get returns the same value")
        void givenEmptyCache_whenPut_thenGetReturnsValue() {
            CaffeineResolutionCache cache = new CaffeineResolutionCache();
            String key = "k1";
            String url = "http://a.example/landing";
            cache.put(key, url, 60);
            String actual = cache.get(key);
            assertThat(actual).isEqualTo(url);
        }

        @Test
        @DisplayName("When two different keys are stored then they are independent")
        void givenEmptyCache_whenPutTwoDifferentKeys_thenIndependent() {
            CaffeineResolutionCache cache = new CaffeineResolutionCache();
            cache.put("k1", "http://one", 60);
            cache.put("k2", "http://two", 60);
            assertThat(cache.get("k1")).isEqualTo("http://one");
            assertThat(cache.get("k2")).isEqualTo("http://two");
        }
    }

    @Nested
    @DisplayName("Given an existing entry")
    class GivenExistingEntry {

        @Test
        @DisplayName("When put is called with the same key then it overrides previous value")
        void givenExistingEntry_whenPutSameKey_thenOverrides() {
            CaffeineResolutionCache cache = new CaffeineResolutionCache();
            String key = "k";
            cache.put(key, "http://first", 60);
            cache.put(key, "http://second", 60);
            assertThat(cache.get(key)).isEqualTo("http://second");
        }
    }

    @Nested
    @DisplayName("Given ttlSeconds parameter is ignored by implementation")
    class GivenTtlIgnoredInImplementation {

        @Test
        @DisplayName("When put is called with any ttl then value is immediately available")
        void givenTtlIgnored_whenPutWithAnyTtl_thenImmediateAvailability() {
            CaffeineResolutionCache cache = new CaffeineResolutionCache();
            cache.put("k", "http://value", 1);
            assertThat(cache.get("k")).isEqualTo("http://value");
        }
    }
}
