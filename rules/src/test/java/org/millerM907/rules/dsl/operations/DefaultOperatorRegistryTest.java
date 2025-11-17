package org.millerM907.rules.dsl.operations;

import org.millerM907.rules.dsl.operations.base.Operator;
import org.millerM907.rules.dsl.operations.impl.DefaultOperatorRegistry;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("DefaultOperatorRegistry — unit tests")
class DefaultOperatorRegistryTest {

    private static Operator op(String name) {
        return new Operator() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public boolean test(Object actual, Object expected) {
                return true;
            }
        };
    }

    @Nested
    @DisplayName("Given non-empty operator list")
    class GivenNonEmptyList {

        @Test
        @DisplayName("When getting existing operator then it is returned")
        void givenList_whenGetExisting_thenReturned() {
            var reg = new DefaultOperatorRegistry(List.of(op("eq"), op("in")));


            Operator got = reg.get("eq");


            assertThat(got).isNotNull();
            assertThat(got.name()).isEqualTo("eq");
        }

        @Test
        @DisplayName("When duplicate names are provided then the first one wins")
        void givenDuplicates_whenConstruct_thenFirstWins() {
            Operator first = op("eq");
            Operator second = new Operator() {
                @Override
                public String name() {
                    return "eq";
                }

                @Override
                public boolean test(Object a, Object b) {
                    return false;
                }
            };
            var reg = new DefaultOperatorRegistry(List.of(first, second));


            Operator got = reg.get("eq");


            assertThat(got).isSameAs(first);
        }
    }

    @Nested
    @DisplayName("Given empty or null operator list")
    class GivenEmptyOrNullList {

        @Test
        @DisplayName("When list is empty then get unknown throws IllegalArgumentException")
        void givenEmpty_whenGetUnknown_thenThrows() {
            var reg = new DefaultOperatorRegistry(List.of());


            assertThatThrownBy(() -> reg.get("missing"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Unknown operator");
        }

        @Test
        @DisplayName("When list is null then registry behaves as empty and get throws")
        void givenNull_whenGetUnknown_thenThrows() {
            var reg = new DefaultOperatorRegistry(null);


            assertThatThrownBy(() -> reg.get("anything"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Unknown operator");
        }
    }

    @Nested
    @DisplayName("Given case sensitivity")
    class GivenCaseSensitivity {

        @Test
        @DisplayName("When name differs only by case then it is treated as different and get fails")
        void givenCaseDifference_whenGet_thenThrows() {
            var reg = new DefaultOperatorRegistry(List.of(op("eq")));


            assertThatThrownBy(() -> reg.get("EQ"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Unknown operator");
        }

        @Test
        @DisplayName("When both casings are registered then both can be retrieved")
        void givenBothCasings_whenGetBoth_thenBothResolved() {
            var reg = new DefaultOperatorRegistry(List.of(op("eq"), op("EQ")));


            Assertions.assertThat(reg.get("eq").name()).isEqualTo("eq");
            Assertions.assertThat(reg.get("EQ").name()).isEqualTo("EQ");
        }
    }
}
