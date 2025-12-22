package org.millerM907.edge.core.context_aggregator.extractors;

import org.millerM907.edge.core.context_aggregator.extractors.impl.BrowserExtractor;
import org.millerM907.edge.core.support.base.BrowserDetector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("BrowserExtractor — unit tests")
class BrowserExtractorTest {

    private final BrowserDetector detector = mock(BrowserDetector.class);
    private final BrowserExtractor extractor = new BrowserExtractor(detector);

    private static ServerHttpRequest reqWithHeaders(String demoBrowser, String userAgent) {
        ServerHttpRequest req = mock(ServerHttpRequest.class);
        HttpHeaders headers = new HttpHeaders();
        if (demoBrowser != null) headers.add("X-Demo-Browser", demoBrowser);
        if (userAgent != null) headers.add("User-Agent", userAgent);
        when(req.getHeaders()).thenReturn(headers);
        return req;
    }

    @Nested
    @DisplayName("Given a non-blank X-Demo-Browser header")
    class GivenNonBlankOverride {

        @Test
        @DisplayName("When extract is called then override value is returned and detector is not used")
        void givenOverride_whenExtract_thenReturnsOverrideAndSkipsDetector() {
            ServerHttpRequest req = reqWithHeaders("Chrome", "UA-IGNORED");

            Map<String, String> out = extractor.extract(req);

            assertThat(out).containsEntry("browser", "Chrome");
            verifyNoInteractions(detector);
        }
    }

    @Nested
    @DisplayName("Given a blank X-Demo-Browser header")
    class GivenBlankOverride {

        @Test
        @DisplayName("When header is empty string then detector is used with User-Agent")
        void givenEmptyOverride_whenExtract_thenUsesDetector() {
            ServerHttpRequest req = reqWithHeaders("", "Mozilla/5.0 Firefox");
            when(detector.detect("Mozilla/5.0 Firefox")).thenReturn("Firefox");

            Map<String, String> out = extractor.extract(req);

            assertThat(out).containsEntry("browser", "Firefox");
            verify(detector).detect("Mozilla/5.0 Firefox");
        }

        @Test
        @DisplayName("When header is whitespace then detector is used with User-Agent")
        void givenWhitespaceOverride_whenExtract_thenUsesDetector() {
            ServerHttpRequest req = reqWithHeaders("   ", "Mozilla/5.0 Safari");
            when(detector.detect("Mozilla/5.0 Safari")).thenReturn("Safari");

            Map<String, String> out = extractor.extract(req);

            assertThat(out).containsEntry("browser", "Safari");
            verify(detector).detect("Mozilla/5.0 Safari");
        }
    }

    @Nested
    @DisplayName("Given no X-Demo-Browser header")
    class GivenNoOverride {

        @Test
        @DisplayName("When User-Agent is present then detector result is returned")
        void givenNoOverride_whenUaPresent_thenReturnsDetectorResult() {
            ServerHttpRequest req = reqWithHeaders(null, "Mozilla/5.0 Chrome");
            when(detector.detect("Mozilla/5.0 Chrome")).thenReturn("Chrome");

            Map<String, String> out = extractor.extract(req);

            assertThat(out).containsEntry("browser", "Chrome");
            verify(detector).detect("Mozilla/5.0 Chrome");
        }

        @Test
        @DisplayName("When User-Agent is present but detector returns null then 'Unknown' is returned")
        void givenNoOverride_whenUaPresentAndDetectorNull_thenUnknown() {
            ServerHttpRequest req = reqWithHeaders(null, "Some-UA");
            when(detector.detect("Some-UA")).thenReturn(null);

            Map<String, String> out = extractor.extract(req);

            assertThat(out).containsEntry("browser", "Unknown");
            verify(detector).detect("Some-UA");
        }

        @Test
        @DisplayName("When User-Agent is missing then detector is called with null and 'Unknown' is returned")
        void givenNoOverride_whenUaMissing_thenDetectorCalledWithNullAndUnknown() {
            ServerHttpRequest req = reqWithHeaders(null, null);
            when(detector.detect(null)).thenReturn(null);

            Map<String, String> out = extractor.extract(req);

            assertThat(out).containsEntry("browser", "Unknown");
            verify(detector).detect(null);
        }
    }
}
