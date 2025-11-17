package org.millerM907.rules.dsl.operations;

import org.millerM907.rules.dsl.operations.impl.InOperator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("InOperator — unit tests")
class InOperatorTest {

    private final InOperator op = new InOperator();

    @Test
    @DisplayName("When name is requested then returns 'in'")
    void givenOperator_whenName_thenIn() {
        assertThat(op.name()).isEqualTo("in");
    }

    @Nested
    @DisplayName("Given collection of candidates")
    class GivenCollection {

        @Test
        @DisplayName("When actual is contained in list then returns true")
        void givenList_whenActualContained_thenTrue() {
            assertThat(op.test("Chrome", List.of("Firefox", "Chrome", "Safari"))).isTrue();
        }

        @Test
        @DisplayName("When actual is not contained in list then returns false")
        void givenList_whenActualNotContained_thenFalse() {
            assertThat(op.test("Edge", List.of("Firefox", "Chrome", "Safari"))).isFalse();
        }

        @Test
        @DisplayName("When collection is empty then returns false")
        void givenEmptyCollection_whenTest_thenFalse() {
            assertThat(op.test("x", List.of())).isFalse();
        }

        @Test
        @DisplayName("When using set then membership is still respected")
        void givenSet_whenActualContained_thenTrue() {
            assertThat(op.test("mobile", Set.of("mobile", "desktop"))).isTrue();
        }

        @Test
        @DisplayName("When collection has mixed types then String.valueOf is used for comparison")
        void givenMixedTypes_whenStringValueMatches_thenTrue() {
            assertThat(op.test(123, List.of("00123", 123, 456L))).isTrue();
        }
    }

    @Nested
    @DisplayName("Given non-collection expected")
    class GivenNonCollection {

        @Test
        @DisplayName("When expected is a string then always returns false")
        void givenStringExpected_whenTest_thenFalse() {
            assertThat(op.test("Chrome", "Chrome,Firefox")).isFalse();
        }

        @Test
        @DisplayName("When expected is null then returns false")
        void givenNullExpected_whenTest_thenFalse() {
            assertThat(op.test("Chrome", null)).isFalse();
        }
    }

    @Nested
    @DisplayName("Given null and edge values")
    class GivenNullAndEdgeValues {

        @Test
        @DisplayName("When actual is null and collection contains null then comparison via String.valueOf results in true")
        void givenNullActualAndNullElement_whenTest_thenTrue() {
            assertThat(op.test(null, Collections.singletonList(null))).isTrue();
        }

        @Test
        @DisplayName("When actual is null and collection has only non-null values then returns false")
        void givenNullActualAndNoNullInCollection_whenTest_thenFalse() {
            assertThat(op.test(null, List.of("a", "b"))).isFalse();
        }

        @Test
        @DisplayName("When collection contains null and actual is non-null then nulls are skipped")
        void givenNonNullActualAndNullElement_whenTest_thenResultDependsOnOthers() {
            assertThat(op.test("x", java.util.Arrays.asList(null, "x"))).isTrue();
            assertThat(op.test("y", java.util.Arrays.asList(null, "x"))).isFalse();
        }
    }
}


