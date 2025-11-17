package org.millerM907.edge.core.support;

import org.millerM907.edge.core.support.impl.SystemTimeProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SystemTimeProvider — unit tests")
class SystemTimeProviderTest {

    private final SystemTimeProvider provider = new SystemTimeProvider();

    @Nested
    @DisplayName("Given the system clock")
    class GivenSystemClock {

        @Test
        @DisplayName("When now is called then a non-null LocalTime is returned")
        void givenSystemClock_whenNow_thenNonNull() {
            LocalTime t = provider.now();
            assertThat(t).isNotNull();
        }

        @Test
        @DisplayName("When now is called then the value lies within the call window")
        void givenSystemClock_whenNow_thenWithinCallWindow() {
            LocalTime before = LocalTime.now();
            LocalTime value  = provider.now();
            LocalTime after  = LocalTime.now();

            boolean withinSameDay = !value.isBefore(before) && !value.isAfter(after);
            boolean wrapAround = after.isBefore(before) && (
                    !value.isBefore(before) || !value.isAfter(after)
            );

            assertThat(withinSameDay || wrapAround).isTrue();
        }
    }
}
