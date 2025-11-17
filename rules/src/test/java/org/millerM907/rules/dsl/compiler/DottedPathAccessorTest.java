package org.millerM907.rules.dsl.compiler;

import org.millerM907.rules.dsl.compiler.impl.DottedPathAccessor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("DottedPathAccessor — unit tests")
class DottedPathAccessorTest {

    private final DottedPathAccessor accessor = new DottedPathAccessor();

    @Nested
    @DisplayName("Given a nested map")
    class GivenNestedMap {

        @Test
        @DisplayName("When reading an existing leaf then the value is returned")
        void givenNested_whenReadExistingLeaf_thenValueReturned() {
            Map<String, Object> ctx = Map.of(
                    "geo", Map.of("country", "NL", "city", "Amsterdam"),
                    "device", "desktop"
            );


            assertThat(accessor.read(ctx, "geo.country")).isEqualTo("NL");
            assertThat(accessor.read(ctx, "device")).isEqualTo("desktop");
        }

        @Test
        @DisplayName("When a key is missing then null is returned")
        void givenNested_whenMissingKey_thenNull() {
            Map<String, Object> ctx = Map.of("geo", Map.of("country", "NL"));


            assertThat(accessor.read(ctx, "geo.region")).isNull();
            assertThat(accessor.read(ctx, "device")).isNull();
        }

        @Test
        @DisplayName("When intermediate value is not a map then null is returned")
        void givenNested_whenIntermediateNotMap_thenNull() {
            Map<String, Object> ctx = Map.of("geo", "NL");


            assertThat(accessor.read(ctx, "geo.country")).isNull();
        }
    }

    @Nested
    @DisplayName("Given edge cases")
    class GivenEdgeCases {

        @Test
        @DisplayName("When context is null then null is returned")
        void givenNullContext_whenRead_thenNull() {
            assertThat(accessor.read(null, "any.path")).isNull();
        }

        @Test
        @DisplayName("When path is null then NullPointerException is thrown")
        void givenNullPath_whenRead_thenNpe() {
            Map<String, Object> ctx = Map.of("a", Map.of("b", "c"));


            assertThatThrownBy(() -> accessor.read(ctx, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("When path is empty then null is returned")
        void givenEmptyPath_whenRead_thenNull() {
            Map<String, Object> ctx = Map.of("a", "x");


            assertThat(accessor.read(ctx, "")).isNull();
        }

        @Test
        @DisplayName("When key contains dot as a flat key then null is returned")
        void givenFlatKeyWithDot_whenRead_thenNull() {
            Map<String, Object> ctx = Map.of("a.b", "value");


            assertThat(accessor.read(ctx, "a.b")).isNull();
        }
    }

    @Nested
    @DisplayName("Given single-level map")
    class GivenSingleLevelMap {

        @Test
        @DisplayName("When reading existing top-level key then the value is returned")
        void givenSingleLevel_whenReadTopLevel_thenValue() {
            Map<String, Object> ctx = Map.of("browser", "Chrome");


            assertThat(accessor.read(ctx, "browser")).isEqualTo("Chrome");
        }
    }
}
