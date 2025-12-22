package org.millerM907.rules.repository;

import org.millerM907.rules.dsl.conditions.*;
import org.millerM907.rules.repository.impl.ClasspathYamlLoader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ResourceLoader;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("ClasspathYamlLoader — unit tests")
class ClasspathYamlLoaderTest {

    private static ClasspathYamlLoader loaderFromString(String yaml) {
        ResourceLoader rl = mock(ResourceLoader.class);
        ByteArrayResource res = new ByteArrayResource(yaml.getBytes(StandardCharsets.UTF_8)) {
            @Override
            public String getDescription() {
                return "in-memory rules.yaml";
            }
        };
        when(rl.getResource(anyString())).thenReturn(res);
        return new ClasspathYamlLoader(rl, "classpath:rules.yaml");
    }

    @Nested
    @DisplayName("Given a valid YAML with links and rules")
    class GivenValidYaml {

        @Test
        @DisplayName("When load is called then RuleSets and Rules are mapped correctly")
        void givenValid_whenLoad_thenMapped() {
            String yaml = """
                    links:
                      - slug: sale1111
                        defaultUrl: "http://default"
                        rules:
                          - id: R1
                            priority: 10
                            when:
                              all:
                                - { path: "browser", operator: "eq", value: "Chrome" }
                                - { path: "device",  operator: "eq", value: "desktop" }
                            then:
                              url: "http://a"
                              ttl: 120
                              reason: "A"
                          - id: R2
                            priority: 5
                            when:
                              any:
                                - { path: "time", operator: "eq", value: "10:00" }
                                - { path: "country", operator: "eq", value: "NL" }
                            then:
                              url: "http://b"
                              ttl: 60
                    """;
            var loader = loaderFromString(yaml);

            DslRoot root = loader.load();

            assertThat(root).isNotNull();
            assertThat(root.links()).hasSize(1);

            RuleSet rs = root.links().get(0);
            assertThat(rs.slug()).isEqualTo("sale1111");
            assertThat(rs.defaultUrl()).isEqualTo("http://default");
            assertThat(rs.rules()).hasSize(2);

            RuleDef r1 = rs.rules().get(0);
            assertThat(r1.id()).isEqualTo("R1");
            assertThat(r1.priority()).isEqualTo(10);
            assertThat(r1.when()).isInstanceOf(AllDef.class);
            assertThat(((AllDef) r1.when()).all()).hasSize(2);

            ThenDef t1 = r1.then();
            assertThat(t1.url()).isEqualTo("http://a");
            assertThat(t1.ttl()).isEqualTo(120);
            assertThat(t1.reason()).isEqualTo("A");

            RuleDef r2 = rs.rules().get(1);
            assertThat(r2.when()).isInstanceOf(AnyDef.class);
            assertThat(((AnyDef) r2.when()).any()).hasSize(2);
        }

        @Test
        @DisplayName("When rule uses 'not' then it is parsed as NotDef with inner predicate")
        void givenNot_whenLoad_thenParsed() {
            String yaml = """
                    links:
                      - slug: s
                        rules:
                          - id: R
                            when:
                              not:
                                path: "browser"
                                operator: "eq"
                                value: "Firefox"
                            then:
                              url: "http://ok"
                    """;
            var loader = loaderFromString(yaml);

            DslRoot root = loader.load();

            RuleDef r = root.links().get(0).rules().get(0);
            assertThat(r.when()).isInstanceOf(NotDef.class);
            assertThat(((NotDef) r.when()).not()).isInstanceOf(PredDef.class);
            ThenDef t = r.then();
            assertThat(t.url()).isEqualTo("http://ok");
        }
    }

    @Nested
    @DisplayName("Given edge cases in YAML structure")
    class GivenEdgeCases {

        @Test
        @DisplayName("When links is empty array then DslRoot contains empty list")
        void givenEmptyLinks_whenLoad_thenEmpty() {
            String yaml = "links: []";
            var loader = loaderFromString(yaml);

            DslRoot root = loader.load();

            assertThat(root.links()).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("When links key is missing then DslRoot contains empty list")
        void givenNoLinks_whenLoad_thenEmpty() {
            String yaml = "someOtherRoot: true";
            var loader = loaderFromString(yaml);

            DslRoot root = loader.load();

            assertThat(root.links()).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("When rule has missing 'then' then ThenDef fields are null")
        void givenMissingThen_whenLoad_thenThenNulls() {
            String yaml = """
                    links:
                      - slug: s
                        rules:
                          - id: R1
                            priority: 1
                            when:
                              path: "browser"
                              operator: "eq"
                              value: "Chrome"
                    """;
            var loader = loaderFromString(yaml);

            DslRoot root = loader.load();

            ThenDef t = root.links().get(0).rules().get(0).then();
            assertThat(t.url()).isNull();
            assertThat(t.ttl()).isNull();
            assertThat(t.reason()).isNull();
        }

        @Test
        @DisplayName("When priority is a string number then it is parsed as integer")
        void givenStringPriority_whenLoad_thenParsedAsInt() {
            String yaml = """
                    links:
                      - slug: s
                        rules:
                          - id: R1
                            priority: "15"
                            when: { path: "k", operator: "eq", value: "v" }
                            then: { url: "http://x" }
                    """;
            var loader = loaderFromString(yaml);

            DslRoot root = loader.load();

            Integer pr = root.links().get(0).rules().get(0).priority();
            assertThat(pr).isEqualTo(15);
        }

        @Test
        @DisplayName("When 'when' is null then it becomes an empty AllDef")
        void givenNullWhen_whenLoad_thenEmptyAllDef() {
            String yaml = """
                    links:
                      - slug: s
                        rules:
                          - id: R1
                            when:
                            then: { url: "http://x" }
                    """;
            var loader = loaderFromString(yaml);

            DslRoot root = loader.load();

            CondDef cd = root.links().get(0).rules().get(0).when();
            assertThat(cd).isInstanceOf(AllDef.class);
            assertThat(((AllDef) cd).all()).isEmpty();
        }

        @Test
        @DisplayName("When rule list is missing then rules defaults to empty list")
        void givenNoRules_whenLoad_thenEmptyRules() {
            String yaml = """
                    links:
                      - slug: s
                        defaultUrl: "http://d"
                    """;
            var loader = loaderFromString(yaml);

            DslRoot root = loader.load();

            RuleSet rs = root.links().get(0);
            assertThat(rs.rules()).isNotNull().isEmpty();
            assertThat(rs.defaultUrl()).isEqualTo("http://d");
        }
    }

    @Nested
    @DisplayName("Given invalid YAML")
    class GivenInvalidYaml {

        @Test
        @DisplayName("When YAML cannot be parsed then IllegalStateException is thrown")
        void givenInvalidYaml_whenLoad_thenThrows() {
            String yaml = "::: this is not yaml :::";
            var loader = loaderFromString(yaml);

            assertThatThrownBy(loader::load)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Failed to load DSL");
        }
    }

    @Nested
    @DisplayName("Given multiple links")
    class GivenMultipleLinks {

        @Test
        @DisplayName("When several links exist then all are loaded with their defaults")
        void givenSeveralLinks_whenLoad_thenAllLoaded() {
            String yaml = """
                    links:
                      - slug: a
                        defaultUrl: "http://da"
                        rules: []
                      - slug: b
                        defaultUrl: "http://db"
                        rules:
                          - id: R1
                            when: { path: "k", operator: "eq", value: "v" }
                            then: { url: "http://x", ttl: 10, reason: "X" }
                    """;
            var loader = loaderFromString(yaml);

            DslRoot root = loader.load();

            assertThat(root.links()).extracting(RuleSet::slug).containsExactly("a", "b");
            RuleSet a = root.links().get(0);
            RuleSet b = root.links().get(1);
            assertThat(a.defaultUrl()).isEqualTo("http://da");
            assertThat(a.rules()).isEmpty();
            assertThat(b.rules()).hasSize(1);
            ThenDef t = b.rules().get(0).then();
            assertThat(t.url()).isEqualTo("http://x");
            assertThat(t.ttl()).isEqualTo(10);
            assertThat(t.reason()).isEqualTo("X");
        }
    }
}
