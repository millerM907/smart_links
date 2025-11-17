package org.millerM907.edge.core.context_aggregator.extractors;

import org.millerM907.edge.core.context_aggregator.extractors.impl.TimeZoneExtractor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("TimeZoneExtractor — unit tests")
class TimeZoneExtractorTest {

    private final TimeZoneExtractor extractor = new TimeZoneExtractor();

    private static ServerHttpRequest reqWithHeader(String value) {
        ServerHttpRequest req = mock(ServerHttpRequest.class);
        HttpHeaders headers = new HttpHeaders();
        if (value != null) headers.add("X-Demo-Tz", value);
        when(req.getHeaders()).thenReturn(headers);
        return req;
    }

    @Nested
    @DisplayName("Given no X-Demo-Tz header")
    class GivenNoHeader {
        @Test
        @DisplayName("When extract is called then returns tz=UTC")
        void givenNoHeader_whenExtract_thenUtc() {
            ServerHttpRequest req = reqWithHeader(null);

            Map<String, String> out = extractor.extract(req);

            assertThat(out).containsEntry("tz", "UTC");
        }
    }

    @Nested
    @DisplayName("Given blank X-Demo-Tz header")
    class GivenBlankHeader {
        @Test
        @DisplayName("When header is empty string then returns tz=UTC")
        void givenEmpty_whenExtract_thenUtc() {
            ServerHttpRequest req = reqWithHeader("");

            Map<String, String> out = extractor.extract(req);

            assertThat(out).containsEntry("tz", "UTC");
        }

        @Test
        @DisplayName("When header is whitespace then returns tz=UTC")
        void givenWhitespace_whenExtract_thenUtc() {
            ServerHttpRequest req = reqWithHeader("   ");

            Map<String, String> out = extractor.extract(req);

            assertThat(out).containsEntry("tz", "UTC");
        }
    }

    @Nested
    @DisplayName("Given non-blank X-Demo-Tz header")
    class GivenNonBlankHeader {
        @Test
        @DisplayName("When header is 'Europe/Amsterdam' then returns tz=Europe/Amsterdam")
        void givenValidTz_whenExtract_thenPassThrough() {
            ServerHttpRequest req = reqWithHeader("Europe/Amsterdam");

            Map<String, String> out = extractor.extract(req);

            assertThat(out).containsEntry("tz", "Europe/Amsterdam");
        }

        @Test
        @DisplayName("When header has surrounding spaces then value is preserved as-is")
        void givenSpacedValue_whenExtract_thenPreserved() {
            ServerHttpRequest req = reqWithHeader(" UTC ");

            Map<String, String> out = extractor.extract(req);

            assertThat(out).containsEntry("tz", " UTC ");
        }
    }
}
