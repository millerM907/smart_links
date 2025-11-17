package org.millerM907.rules.repository;

import org.millerM907.rules.dsl.conditions.DslRoot;
import org.millerM907.rules.dsl.conditions.RuleSet;
import org.millerM907.rules.repository.base.DslLoader;
import org.millerM907.rules.repository.impl.InMemoryRuleSetRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("InMemoryRuleSetRepository — unit tests")
class InMemoryRuleSetRepositoryTest {

    static final class StubLoader implements DslLoader {
        DslRoot root;
        StubLoader(DslRoot root) { this.root = root; }
        @Override public DslRoot load() { return root; }
    }

    private static DslRoot root(RuleSet... sets) {
        return new DslRoot(List.of(sets));
    }

    private static RuleSet set(String slug, String defUrl) {
        return new RuleSet(slug, defUrl, List.of());
    }

    @Nested
    @DisplayName("Given a valid DSL on startup")
    class GivenValidOnStartup {

        @Test
        @DisplayName("When repository is constructed then it loads index and bumps version")
        void givenValid_whenConstruct_thenIndexAndVersion() {
            var loader = new StubLoader(root(set("a", "u1"), set("b", "u2")));
            var repo = new InMemoryRuleSetRepository(loader);


            assertThat(repo.version()).isEqualTo(1L);
            assertThat(repo.current("a").defaultUrl()).isEqualTo("u1");
            assertThat(repo.current("b").defaultUrl()).isEqualTo("u2");
        }
    }

    @Nested
    @DisplayName("Given queries for slugs")
    class GivenSlugQueries {

        @Test
        @DisplayName("When slug does not exist then IllegalArgumentException is thrown")
        void givenUnknownSlug_whenCurrent_thenThrows() {
            var loader = new StubLoader(root(set("x", "u")));
            var repo = new InMemoryRuleSetRepository(loader);


            assertThatThrownBy(() -> repo.current("absent"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Unknown slug");
        }
    }

    @Nested
    @DisplayName("Given a reload with new data")
    class GivenReload {

        @Test
        @DisplayName("When reload is called then index is replaced and version increments")
        void givenNewData_whenReload_thenReplacedAndVersionBumped() {
            var loader = new StubLoader(root(set("a", "u1")));
            var repo = new InMemoryRuleSetRepository(loader);
            long v1 = repo.version();


            loader.root = root(set("b", "u2"));
            repo.reload();
            long v2 = repo.version();


            assertThat(v2).isEqualTo(v1 + 1);
            assertThatThrownBy(() -> repo.current("a")).isInstanceOf(IllegalArgumentException.class);
            assertThat(repo.current("b").defaultUrl()).isEqualTo("u2");
        }

        @Test
        @DisplayName("When reload returns null root then index becomes empty and version increments")
        void givenNullRoot_whenReload_thenEmptyAndVersionBumped() {
            var loader = new StubLoader(root(set("a", "u1")));
            var repo = new InMemoryRuleSetRepository(loader);
            long v1 = repo.version();


            loader.root = null;
            repo.reload();
            long v2 = repo.version();


            assertThat(v2).isEqualTo(v1 + 1);
            assertThatThrownBy(() -> repo.current("a")).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("When reload returns root with null links then index becomes empty")
        void givenNullLinks_whenReload_thenEmpty() {
            var loader = new StubLoader(new DslRoot(null));
            var repo = new InMemoryRuleSetRepository(loader);

            assertThatThrownBy(() -> repo.current("any")).isInstanceOf(IllegalArgumentException.class);
            long v1 = repo.version();

            loader.root = new DslRoot(null);
            repo.reload();

            assertThat(repo.version()).isEqualTo(v1 + 1);
            assertThatThrownBy(() -> repo.current("any")).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("When links contain a null slug then it is skipped")
        void givenNullSlug_whenReload_thenSkipped() {
            var withNullSlug = new RuleSet(null, "u", List.of());
            var loader = new StubLoader(root(withNullSlug, set("ok", "u2")));
            var repo = new InMemoryRuleSetRepository(loader);


            assertThat(repo.current("ok").defaultUrl()).isEqualTo("u2");
            assertThatThrownBy(() -> repo.current(null)).isInstanceOf(IllegalArgumentException.class);
        }
    }
}
