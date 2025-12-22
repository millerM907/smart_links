package org.millerM907.rules.dsl.operations;

import org.millerM907.rules.dsl.operations.impl.RegexOperator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.regex.PatternSyntaxException;

import static org.assertj.core.api.Assertions.*;

@DisplayName("RegexOperator — unit tests")
class RegexOperatorTest {

    private final RegexOperator op = new RegexOperator();

    @Test
    @DisplayName("When name is requested then returns 'regex'")
    void givenOperator_whenName_thenRegex() {
        assertThat(op.name()).isEqualTo("regex");
    }

    @Nested
    @DisplayName("Given valid pattern and input")
    class GivenValidPattern {

        @Test
        @DisplayName("When input matches pattern then returns true")
        void givenMatchingInput_whenTest_thenTrue() {
            assertThat(op.test("Chrome-120", "Chrome-\\d+")).isTrue();
        }

        @Test
        @DisplayName("When input does not match pattern then returns false")
        void givenNonMatchingInput_whenTest_thenFalse() {
            assertThat(op.test("Firefox", "Chrome.*")).isFalse();
        }

        @Test
        @DisplayName("When pattern is wildcard then any input matches")
        void givenWildcardPattern_whenTest_thenTrue() {
            assertThat(op.test("anything", ".*")).isTrue();
            assertThat(op.test("", ".*")).isTrue();
        }

        @Test
        @DisplayName("When pattern uses anchors then full string is matched")
        void givenAnchoredPattern_whenTest_thenFullMatchRequired() {
            assertThat(op.test("abc", "^abc$")).isTrue();
            assertThat(op.test("xabc", "^abc$")).isFalse();
            assertThat(op.test("abcx", "^abc$")).isFalse();
        }

        @Test
        @DisplayName("When pattern has escaped special characters then they are treated literally")
        void givenEscapedSpecialChars_whenTest_thenLiteralMatch() {
            assertThat(op.test("file.name.txt", "file\\.name\\.txt")).isTrue();
            assertThat(op.test("fileXnameXtxt", "file\\.name\\.txt")).isFalse();
        }

        @Test
        @DisplayName("When pattern is empty string then only empty input matches")
        void givenEmptyPattern_whenTest_thenOnlyEmptyMatches() {
            assertThat(op.test("", "")).isTrue();
            assertThat(op.test("x", "")).isFalse();
        }
    }

    @Nested
    @DisplayName("Given null values")
    class GivenNullValues {

        @Test
        @DisplayName("When actual is null then returns false")
        void givenNullActual_whenTest_thenFalse() {
            assertThat(op.test(null, ".*")).isFalse();
        }

        @Test
        @DisplayName("When expected is null then returns false")
        void givenNullExpected_whenTest_thenFalse() {
            assertThat(op.test("Chrome", null)).isFalse();
        }

        @Test
        @DisplayName("When both actual and expected are null then returns false")
        void givenBothNull_whenTest_thenFalse() {
            assertThat(op.test(null, null)).isFalse();
        }
    }

    @Nested
    @DisplayName("Given invalid pattern")
    class GivenInvalidPattern {

        @Test
        @DisplayName("When pattern is syntactically invalid then PatternSyntaxException is thrown")
        void givenInvalidPattern_whenTest_thenThrows() {
            assertThatThrownBy(() -> op.test("abc", "*invalid["))
                    .isInstanceOf(PatternSyntaxException.class);
        }
    }

    @Nested
    @DisplayName("Given non-string inputs")
    class GivenNonStringInputs {

        @Test
        @DisplayName("When actual and expected are non-strings then String.valueOf is used")
        void givenNonStrings_whenTest_thenStringValueUsed() {
            assertThat(op.test(12345, "123\\d\\d")).isTrue();
            assertThat(op.test(12345L, "1234")).isFalse();
        }
    }
}
