package org.millerM907.rules.dsl.operations;

import org.millerM907.rules.dsl.operations.impl.BetweenTimeOperator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.format.DateTimeParseException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("BetweenTimeOperator — unit tests")
class BetweenTimeOperatorTest {

    private final BetweenTimeOperator op = new BetweenTimeOperator();

    @Test
    @DisplayName("When name is requested then returns 'between'")
    void givenOperator_whenName_thenBetween() {
        assertThat(op.name()).isEqualTo("between");
    }

    @Nested
    @DisplayName("Given valid HH:mm range")
    class GivenValidRange {

        @Test
        @DisplayName("When time is inside range then returns true")
        void givenInside_whenTest_thenTrue() {
            assertThat(op.test("10:15", "09:00-18:00")).isTrue();
        }

        @Test
        @DisplayName("When time equals start then returns true (inclusive)")
        void givenEqualsStart_whenTest_thenTrue() {
            assertThat(op.test("09:00", "09:00-18:00")).isTrue();
        }

        @Test
        @DisplayName("When time equals end then returns true (inclusive)")
        void givenEqualsEnd_whenTest_thenTrue() {
            assertThat(op.test("18:00", "09:00-18:00")).isTrue();
        }

        @Test
        @DisplayName("When time is before start then returns false")
        void givenBeforeStart_whenTest_thenFalse() {
            assertThat(op.test("08:59", "09:00-18:00")).isFalse();
        }

        @Test
        @DisplayName("When time is after end then returns false")
        void givenAfterEnd_whenTest_thenFalse() {
            assertThat(op.test("18:01", "09:00-18:00")).isFalse();
        }
    }

    @Nested
    @DisplayName("Given nulls")
    class GivenNulls {

        @Test
        @DisplayName("When actual is null then returns false")
        void givenNullActual_whenTest_thenFalse() {
            assertThat(op.test(null, "09:00-18:00")).isFalse();
        }

        @Test
        @DisplayName("When expected is null then returns false")
        void givenNullExpected_whenTest_thenFalse() {
            assertThat(op.test("10:00", null)).isFalse();
        }
    }

    @Nested
    @DisplayName("Given malformed inputs")
    class GivenMalformed {

        @Test
        @DisplayName("When expected has no dash then throws ArrayIndexOutOfBoundsException")
        void givenNoDash_whenTest_thenThrows() {
            assertThatThrownBy(() -> op.test("10:00", "09:00"))
                    .isInstanceOf(ArrayIndexOutOfBoundsException.class);
        }

        @Test
        @DisplayName("When expected times are invalid then throws DateTimeParseException")
        void givenBadExpectedTimes_whenTest_thenThrows() {
            assertThatThrownBy(() -> op.test("10:00", "aa-bb"))
                    .isInstanceOf(DateTimeParseException.class);
        }

        @Test
        @DisplayName("When actual time is invalid then throws DateTimeParseException")
        void givenBadActual_whenTest_thenThrows() {
            assertThatThrownBy(() -> op.test("not-a-time", "09:00-18:00"))
                    .isInstanceOf(DateTimeParseException.class);
        }

        @Test
        @DisplayName("When strings contain spaces then throws DateTimeParseException")
        void givenSpaces_whenTest_thenThrows() {
            assertThatThrownBy(() -> op.test(" 10:00 ", " 09:00 - 18:00 "))
                    .isInstanceOf(DateTimeParseException.class);
        }
    }

    @Nested
    @DisplayName("Given overnight range semantics")
    class GivenOvernight {

        @Test
        @DisplayName("When range crosses midnight then current implementation returns false for early hours")
        void givenCrossMidnight_whenEarlyHour_thenFalse() {
            assertThat(op.test("01:00", "23:00-02:00")).isFalse();
        }
    }
}
