package org.millerM907.edge.core.fingerprint;

import org.millerM907.edge.models.RequestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SimpleFingerprintStrategy — unit tests")
class SimpleFingerprintStrategyTest {

    private final SimpleFingerprintStrategy strategy = new SimpleFingerprintStrategy();

    @Nested
    @DisplayName("Given a null or empty context")
    class GivenNullOrEmptyContext {

        @Test
        @DisplayName("When slug is non-null then result equals slug")
        void givenNullContext_whenSlugNonNull_thenResultEqualsSlug() {
            String fp = strategy.fingerprint("sale1111", null);
            assertThat(fp).isEqualTo("sale1111");
        }

        @Test
        @DisplayName("When slug is null then result is empty string")
        void givenNullContext_whenSlugNull_thenResultIsEmpty() {
            String fp = strategy.fingerprint(null, null);
            assertThat(fp).isEmpty();
        }

        @Test
        @DisplayName("When attrs map is null then result equals slug")
        void givenNullAttrs_whenSlugNonNull_thenResultEqualsSlug() {
            RequestContext ctx = new RequestContext(null);
            String fp = strategy.fingerprint("slug", ctx);
            assertThat(fp).isEqualTo("slug");
        }
    }

    @Nested
    @DisplayName("Given attributes with null keys or values and blank values")
    class GivenInvalidEntries {

        @Test
        @DisplayName("When building fingerprint then null keys, null values and blanks are skipped")
        void givenInvalidEntries_whenBuild_thenSkipsThem() {
            Map<String,String> attrs = new LinkedHashMap<>();
            attrs.put("browser", "Chrome");
            attrs.put(null, "x");
            attrs.put("country", "  ");
            attrs.put("device", null);
            RequestContext ctx = new RequestContext(attrs);

            String fp = strategy.fingerprint("slug", ctx);

            assertThat(fp).isEqualTo("slug|browser=Chrome");
        }
    }

    @Nested
    @DisplayName("Given multiple attributes")
    class GivenMultipleAttributes {

        @Test
        @DisplayName("When building fingerprint then pairs are sorted case-insensitively and deterministic")
        void givenAttributes_whenBuild_thenSortedCaseInsensitive() {
            Map<String,String> attrs = new LinkedHashMap<>();
            attrs.put("tz", "UTC");
            attrs.put("Browser", "Chrome");
            attrs.put("device", "mobile");
            RequestContext ctx = new RequestContext(attrs);

            String fp = strategy.fingerprint("sale1111", ctx);

            assertThat(fp).isEqualTo("sale1111|Browser=Chrome|device=mobile|tz=UTC");
        }

        @Test
        @DisplayName("When keys differ only by case then the last value wins and the first key casing is retained")
        void givenCaseOnlyDifference_whenBuild_thenLastValueWinsFirstCasingRetained() {
            Map<String,String> attrs = new LinkedHashMap<>();
            attrs.put("device", "mobile");
            attrs.put("Device", "tablet");
            attrs.put("DEVICE", "phone");

            RequestContext ctx = new RequestContext(attrs);
            String fp = strategy.fingerprint("slug", ctx);

            assertThat(fp).isEqualTo("slug|device=phone");
        }
    }

    @Nested
    @DisplayName("Given a null slug")
    class GivenNullSlug {

        @Test
        @DisplayName("When attributes exist then result starts with the first '|' followed by sorted pairs")
        void givenNullSlug_whenAttributes_thenStartsWithPipeAndPairs() {
            Map<String,String> attrs = new LinkedHashMap<>();
            attrs.put("b", "2");
            attrs.put("a", "1");
            RequestContext ctx = new RequestContext(attrs);

            String fp = strategy.fingerprint(null, ctx);

            assertThat(fp).isEqualTo("|a=1|b=2");
        }
    }
}
