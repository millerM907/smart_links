package org.millerM907.edge.core.context_aggregator;

import org.millerM907.edge.core.context_aggregator.extractors.base.AttributeExtractor;
import org.millerM907.edge.models.RequestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("DefaultRequestContextAggregator — unit tests")
class DefaultRequestContextAggregatorTest {

    @Nested
    @DisplayName("Given no query params and no extractors")
    class GivenNoParamsNoExtractors {

        @Test
        @DisplayName("When aggregate is called then returns empty context")
        void givenNoParamsNoExtractors_whenAggregate_thenEmpty() {
            ServerHttpRequest req = mock(ServerHttpRequest.class);
            MultiValueMap<String, String> qp = new LinkedMultiValueMap<>();
            when(req.getQueryParams()).thenReturn(qp);

            DefaultRequestContextAggregator agg = new DefaultRequestContextAggregator(List.of());
            RequestContext ctx = agg.aggregate(req);

            assertThat(ctx.attrs()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Given query params with multiple values")
    class GivenQueryParamsMultipleValues {

        @Test
        @DisplayName("When aggregate is called then only the first value is used")
        void givenMultipleValues_whenAggregate_thenFirstOnly() {
            ServerHttpRequest req = mock(ServerHttpRequest.class);
            LinkedMultiValueMap<String, String> qp = new LinkedMultiValueMap<>();
            qp.add("k", "v1");
            qp.add("k", "v2");
            when(req.getQueryParams()).thenReturn(qp);

            DefaultRequestContextAggregator agg = new DefaultRequestContextAggregator(List.of());
            RequestContext ctx = agg.aggregate(req);

            assertThat(ctx.attrs()).containsEntry("k", "v1");
        }

        @Test
        @DisplayName("When first value is null then the key is skipped")
        void givenFirstNull_whenAggregate_thenSkipped() {
            ServerHttpRequest req = mock(ServerHttpRequest.class);
            LinkedMultiValueMap<String, String> qp = new LinkedMultiValueMap<>();
            qp.put("a", new ArrayList<>(Arrays.asList(null, "b")));
            when(req.getQueryParams()).thenReturn(qp);

            DefaultRequestContextAggregator agg = new DefaultRequestContextAggregator(List.of());
            RequestContext ctx = agg.aggregate(req);

            assertThat(ctx.attrs()).doesNotContainKey("a");
        }
    }

    @Nested
    @DisplayName("Given extractors that support or do not support the request")
    class GivenSupportingAndNonSupportingExtractors {

        @Test
        @DisplayName("When only supporting extractors are applied then their pairs are merged")
        void givenSupportingExtractors_whenAggregate_thenMerged() {
            ServerHttpRequest req = mock(ServerHttpRequest.class);
            when(req.getQueryParams()).thenReturn(new LinkedMultiValueMap<>());

            AttributeExtractor ex1 = mock(AttributeExtractor.class);
            when(ex1.supports(req)).thenReturn(true);
            when(ex1.extract(req)).thenReturn(Map.of("a", "1"));

            AttributeExtractor ex2 = mock(AttributeExtractor.class);
            when(ex2.supports(req)).thenReturn(true);
            when(ex2.extract(req)).thenReturn(Map.of("b", "2"));

            DefaultRequestContextAggregator agg = new DefaultRequestContextAggregator(List.of(ex1, ex2));
            RequestContext ctx = agg.aggregate(req);

            assertThat(ctx.attrs()).containsEntry("a", "1").containsEntry("b", "2");
        }

        @Test
        @DisplayName("When non-supporting extractor is present then it is ignored")
        void givenNonSupportingExtractor_whenAggregate_thenIgnored() {
            ServerHttpRequest req = mock(ServerHttpRequest.class);
            when(req.getQueryParams()).thenReturn(new LinkedMultiValueMap<>());

            AttributeExtractor ex1 = mock(AttributeExtractor.class);
            when(ex1.supports(req)).thenReturn(false);
            when(ex1.extract(req)).thenReturn(Map.of("x", "y"));

            DefaultRequestContextAggregator agg = new DefaultRequestContextAggregator(List.of(ex1));
            RequestContext ctx = agg.aggregate(req);

            assertThat(ctx.attrs()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Given key collisions between query params and extractors")
    class GivenKeyCollisions {

        @Test
        @DisplayName("When extractor produces the same key then extractor value overrides query value")
        void givenCollision_whenAggregate_thenExtractorOverrides() {
            ServerHttpRequest req = mock(ServerHttpRequest.class);
            LinkedMultiValueMap<String, String> qp = new LinkedMultiValueMap<>();
            qp.add("k", "fromQuery");
            when(req.getQueryParams()).thenReturn(qp);

            AttributeExtractor ex = mock(AttributeExtractor.class);
            when(ex.supports(req)).thenReturn(true);
            when(ex.extract(req)).thenReturn(Map.of("k", "fromExtractor"));

            DefaultRequestContextAggregator agg = new DefaultRequestContextAggregator(List.of(ex));
            RequestContext ctx = agg.aggregate(req);

            assertThat(ctx.attrs()).containsEntry("k", "fromExtractor");
        }

        @Test
        @DisplayName("When multiple extractors produce the same key then the later extractor wins")
        void givenMultipleExtractorCollisions_whenAggregate_thenLastWins() {
            ServerHttpRequest req = mock(ServerHttpRequest.class);
            when(req.getQueryParams()).thenReturn(new LinkedMultiValueMap<>());

            AttributeExtractor ex1 = mock(AttributeExtractor.class);
            when(ex1.supports(req)).thenReturn(true);
            when(ex1.extract(req)).thenReturn(Map.of("k", "v1"));

            AttributeExtractor ex2 = mock(AttributeExtractor.class);
            when(ex2.supports(req)).thenReturn(true);
            when(ex2.extract(req)).thenReturn(Map.of("k", "v2"));

            DefaultRequestContextAggregator agg = new DefaultRequestContextAggregator(List.of(ex1, ex2));
            RequestContext ctx = agg.aggregate(req);

            assertThat(ctx.attrs()).containsEntry("k", "v2");
        }
    }

    @Nested
    @DisplayName("Given a mix of query params and extractor outputs")
    class GivenMixedSources {

        @Test
        @DisplayName("When aggregate is called then result preserves insertion order within each phase")
        void givenMixed_whenAggregate_thenOrderByPhases() {
            ServerHttpRequest req = mock(ServerHttpRequest.class);
            LinkedMultiValueMap<String, String> qp = new LinkedMultiValueMap<>();
            qp.add("a", "1");
            qp.add("b", "2");
            when(req.getQueryParams()).thenReturn(qp);

            AttributeExtractor ex = mock(AttributeExtractor.class);
            when(ex.supports(req)).thenReturn(true);
            Map<String,String> after = new LinkedHashMap<>();
            after.put("c", "3");
            after.put("d", "4");
            when(ex.extract(req)).thenReturn(after);

            DefaultRequestContextAggregator agg = new DefaultRequestContextAggregator(List.of(ex));
            RequestContext ctx = agg.aggregate(req);

            assertThat(ctx.attrs().keySet()).containsExactly("a", "b", "c", "d");
        }
    }
}