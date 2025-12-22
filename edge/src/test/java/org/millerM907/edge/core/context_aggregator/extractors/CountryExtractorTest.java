package org.millerM907.edge.core.context_aggregator.extractors;

import org.millerM907.edge.core.context_aggregator.extractors.impl.CountryExtractor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("CountryExtractor — unit tests")
class CountryExtractorTest {

    private final CountryExtractor extractor = new CountryExtractor();

    private static ServerHttpRequest requestWithHeader(String headerValue) {
        ServerHttpRequest req = mock(ServerHttpRequest.class);
        HttpHeaders headers = new HttpHeaders();
        if (headerValue != null) {
            headers.add("X-Demo-Geo", headerValue);
        }
        when(req.getHeaders()).thenReturn(headers);
        return req;
    }

    @Nested
    @DisplayName("Given no X-Demo-Geo header")
    class GivenNoHeader {

        @Test
        @DisplayName("When extract is called then returns country=UNKNOWN")
        void givenNoHeader_whenExtract_thenUnknown() {
            ServerHttpRequest req = requestWithHeader(null);

            Map<String, String> out = extractor.extract(req);

            assertThat(out).containsEntry("country", "UNKNOWN");
        }
    }

    @Nested
    @DisplayName("Given blank X-Demo-Geo header")
    class GivenBlankHeader {

        @Test
        @DisplayName("When header is empty string then returns country=UNKNOWN")
        void givenEmpty_whenExtract_thenUnknown() {
            ServerHttpRequest req = requestWithHeader("");

            Map<String, String> out = extractor.extract(req);

            assertThat(out).containsEntry("country", "UNKNOWN");
        }

        @Test
        @DisplayName("When header is whitespace then returns country=UNKNOWN")
        void givenWhitespace_whenExtract_thenUnknown() {
            ServerHttpRequest req = requestWithHeader("   ");

            Map<String, String> out = extractor.extract(req);

            assertThat(out).containsEntry("country", "UNKNOWN");
        }
    }

    @Nested
    @DisplayName("Given non-blank X-Demo-Geo header")
    class GivenNonBlankHeader {

        @Test
        @DisplayName("When header is 'NL' then returns country=NL")
        void givenNl_whenExtract_thenNl() {
            ServerHttpRequest req = requestWithHeader("NL");

            Map<String, String> out = extractor.extract(req);

            assertThat(out).containsEntry("country", "NL");
        }

        @Test
        @DisplayName("When header has surrounding spaces then value is preserved as-is")
        void givenSpacedValue_whenExtract_thenPreserved() {

            ServerHttpRequest req = requestWithHeader(" RU ");

            Map<String, String> out = extractor.extract(req);

            assertThat(out).containsEntry("country", " RU ");
        }

        @Test
        @DisplayName("When header is lowercase then casing is preserved")
        void givenLowercase_whenExtract_thenCasingPreserved() {
            ServerHttpRequest req = requestWithHeader("us");

            Map<String, String> out = extractor.extract(req);

            assertThat(out).containsEntry("country", "us");
        }
    }
}
