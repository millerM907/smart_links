package org.millerM907.edge.core.context_aggregator.extractors;

import org.millerM907.edge.core.context_aggregator.extractors.impl.AttrsPrefixedQueryExtractor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("AttrsPrefixedQueryExtractor — unit tests")
class AttrsPrefixedQueryExtractorTest {

    private final AttrsPrefixedQueryExtractor extractor = new AttrsPrefixedQueryExtractor();

    private static ServerHttpRequest reqWithQuery(MultiValueMap<String, String> qp) {
        ServerHttpRequest req = mock(ServerHttpRequest.class);
        when(req.getQueryParams()).thenReturn(qp);
        return req;
    }

    @Nested
    @DisplayName("Given no query parameters")
    class GivenNoQueryParams {
        @Test
        @DisplayName("When extract is called then returns empty map")
        void givenNoParams_whenExtract_thenEmpty() {
            ServerHttpRequest req = reqWithQuery(new LinkedMultiValueMap<>());

            Map<String, String> out = extractor.extract(req);

            assertThat(out).isEmpty();
        }
    }

    @Nested
    @DisplayName("Given parameters without attrs. prefix")
    class GivenNoAttrsPrefix {
        @Test
        @DisplayName("When extract is called then returns empty map")
        void givenParamsWithoutPrefix_whenExtract_thenEmpty() {
            LinkedMultiValueMap<String, String> qp = new LinkedMultiValueMap<>();
            qp.add("foo", "bar");
            qp.add("x", "y");
            ServerHttpRequest req = reqWithQuery(qp);

            Map<String, String> out = extractor.extract(req);

            assertThat(out).isEmpty();
        }
    }

    @Nested
    @DisplayName("Given parameters with attrs. prefix")
    class GivenAttrsPrefix {

        @Test
        @DisplayName("When single value present then value is mapped without prefix")
        void givenSingleValue_whenExtract_thenMapped() {
            LinkedMultiValueMap<String, String> qp = new LinkedMultiValueMap<>();
            qp.add("attrs.device", "mobile");
            ServerHttpRequest req = reqWithQuery(qp);

            Map<String, String> out = extractor.extract(req);

            assertThat(out).containsEntry("device", "mobile");
        }

        @Test
        @DisplayName("When multiple values present then only the first value is used")
        void givenMultipleValues_whenExtract_thenFirstOnly() {
            LinkedMultiValueMap<String, String> qp = new LinkedMultiValueMap<>();
            qp.add("attrs.color", "red");
            qp.add("attrs.color", "blue");
            ServerHttpRequest req = reqWithQuery(qp);

            Map<String, String> out = extractor.extract(req);

            assertThat(out).containsEntry("color", "red");
        }

        @Test
        @DisplayName("When first value is null then key is skipped")
        void givenFirstNull_whenExtract_thenSkipped() {
            LinkedMultiValueMap<String, String> qp = new LinkedMultiValueMap<>();
            qp.put("attrs.flag", new ArrayList<>(Arrays.asList(null, "true")));
            ServerHttpRequest req = reqWithQuery(qp);

            Map<String, String> out = extractor.extract(req);

            assertThat(out).doesNotContainKey("flag");
        }

        @Test
        @DisplayName("When first value is empty string then empty string is kept")
        void givenFirstEmptyString_whenExtract_thenKept() {
            LinkedMultiValueMap<String, String> qp = new LinkedMultiValueMap<>();
            qp.put("attrs.note", new ArrayList<>(Arrays.asList("", "ignored")));
            ServerHttpRequest req = reqWithQuery(qp);

            Map<String, String> out = extractor.extract(req);

            assertThat(out).containsEntry("note", "");
        }

        @Test
        @DisplayName("When multiple attrs keys then insertion order is preserved")
        void givenMultipleAttrsKeys_whenExtract_thenOrderPreserved() {
            LinkedMultiValueMap<String, String> qp = new LinkedMultiValueMap<>();
            qp.add("attrs.a", "1");
            qp.add("attrs.b", "2");
            qp.add("attrs.c", "3");
            ServerHttpRequest req = reqWithQuery(qp);

            Map<String, String> out = extractor.extract(req);

            assertThat(out.keySet()).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("When key is exactly 'attrs.' then empty name is used as map key")
        void givenBarePrefix_whenExtract_thenEmptyKey() {
            LinkedMultiValueMap<String, String> qp = new LinkedMultiValueMap<>();
            qp.add("attrs.", "val");
            ServerHttpRequest req = reqWithQuery(qp);

            Map<String, String> out = extractor.extract(req);

            assertThat(out).containsEntry("", "val");
        }
    }
}
