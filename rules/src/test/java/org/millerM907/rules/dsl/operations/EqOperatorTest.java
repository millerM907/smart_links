package org.millerM907.rules.dsl.operations;

import org.millerM907.rules.dsl.operations.impl.EqOperator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EqOperator — unit tests")
class EqOperatorTest {

    private final EqOperator op = new EqOperator();

    @Test
    @DisplayName("When name is requested then returns 'eq'")
    void givenOperator_whenName_thenEq() {
        assertThat(op.name()).isEqualTo("eq");
    }

    @Nested
    @DisplayName("Given equal values")
    class GivenEqualValues {

        @Test
        @DisplayName("When both are the same strings then returns true")
        void givenSameStrings_whenTest_thenTrue() {
            assertThat(op.test("Chrome", "Chrome")).isTrue();
        }

        @Test
        @DisplayName("When number and its string match then returns true")
        void givenNumberAndStringSame_whenTest_thenTrue() {
            assertThat(op.test(123, "123")).isTrue();
        }

        @Test
        @DisplayName("When both are null then returns true (via String.valueOf)")
        void givenBothNull_whenTest_thenTrue() {
            assertThat(op.test(null, null)).isTrue();
        }

        @Test
        @DisplayName("When custom object toString equals expected then returns true")
        void givenCustomObject_whenToStringMatches_thenTrue() {
            Object o = new Object() {
                @Override
                public String toString() {
                    return "X";
                }
            };
            assertThat(op.test(o, "X")).isTrue();
        }

        @Test
        @DisplayName("When both are empty strings then returns true")
        void givenEmptyStrings_whenTest_thenTrue() {
            assertThat(op.test("", "")).isTrue();
        }
    }

    @Nested
    @DisplayName("Given non-equal values")
    class GivenNonEqualValues {

        @Test
        @DisplayName("When strings differ then returns false")
        void givenDifferentStrings_whenTest_thenFalse() {
            assertThat(op.test("Chrome", "Firefox")).isFalse();
        }

        @Test
        @DisplayName("When case differs then returns false")
        void givenCaseDifference_whenTest_thenFalse() {
            assertThat(op.test("Chrome", "chrome")).isFalse();
        }

        @Test
        @DisplayName("When null vs empty then returns false")
        void givenNullAndEmpty_whenTest_thenFalse() {
            assertThat(op.test(null, "")).isFalse();
        }

        @Test
        @DisplayName("When object toString does not match then returns false")
        void givenCustomObject_whenToStringDiffers_thenFalse() {
            Object o = new Object() {
                @Override
                public String toString() {
                    return "Y";
                }
            };
            assertThat(op.test(o, "X")).isFalse();
        }
    }
}


