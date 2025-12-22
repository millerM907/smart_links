package org.millerM907.edge.core.context_aggregator.extractors;

import org.millerM907.edge.core.context_aggregator.extractors.impl.TimeExtractor;
import org.millerM907.edge.core.support.base.TimeProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.time.LocalTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("TimeExtractor — unit tests")
class TimeExtractorTest {

    private final TimeProvider timeProvider = mock(TimeProvider.class);
    private final TimeExtractor extractor = new TimeExtractor(timeProvider);

    private static ServerHttpRequest requestWithHeader(String headerValue) {
        ServerHttpRequest req = mock(ServerHttpRequest.class);
        HttpHeaders headers = new HttpHeaders();
        if (headerValue != null) headers.add("X-Demo-Time", headerValue);
        when(req.getHeaders()).thenReturn(headers);
        return req;
    }

    @Nested
    @DisplayName("Given a non-blank X-Demo-Time header")
    class GivenNonBlankHeader {

        @Test
        @DisplayName("When extract is called then the header value is returned and clock is not used")
        void givenHeader_whenExtract_thenReturnsHeaderAndSkipsClock() {
            ServerHttpRequest req = requestWithHeader("10:15");

            Map<String, String> out = extractor.extract(req);

            assertThat(out).containsEntry("time", "10:15");
            verifyNoInteractions(timeProvider);
        }

        @Test
        @DisplayName("When header has surrounding spaces then the value is preserved as-is")
        void givenHeaderWithSpaces_whenExtract_thenPreserved() {
            ServerHttpRequest req = requestWithHeader(" 09:05 ");

            Map<String, String> out = extractor.extract(req);

            assertThat(out).containsEntry("time", " 09:05 ");
            verifyNoInteractions(timeProvider);
        }
    }

    @Nested
    @DisplayName("Given a blank X-Demo-Time header")
    class GivenBlankHeader {

        @Test
        @DisplayName("When header is empty string then current time in HH:mm is used")
        void givenEmptyHeader_whenExtract_thenUsesClock() {
            when(timeProvider.now()).thenReturn(LocalTime.of(7, 3));
            ServerHttpRequest req = requestWithHeader("");

            Map<String, String> out = extractor.extract(req);

            assertThat(out).containsEntry("time", "07:03");
            verify(timeProvider).now();
        }

        @Test
        @DisplayName("When header is whitespace then current time in HH:mm is used")
        void givenWhitespaceHeader_whenExtract_thenUsesClock() {
            when(timeProvider.now()).thenReturn(LocalTime.of(23, 59));
            ServerHttpRequest req = requestWithHeader("   ");

            Map<String, String> out = extractor.extract(req);

            assertThat(out).containsEntry("time", "23:59");
            verify(timeProvider).now();
        }
    }

    @Nested
    @DisplayName("Given no X-Demo-Time header")
    class GivenNoHeader {

        @Test
        @DisplayName("When extract is called then current time in HH:mm is used")
        void givenNoHeader_whenExtract_thenUsesClock() {
            when(timeProvider.now()).thenReturn(LocalTime.of(0, 0));
            ServerHttpRequest req = requestWithHeader(null);

            Map<String, String> out = extractor.extract(req);

            assertThat(out).containsEntry("time", "00:00");
            verify(timeProvider).now();
        }
    }
}
