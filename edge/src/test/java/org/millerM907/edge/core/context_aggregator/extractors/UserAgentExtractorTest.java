package org.millerM907.edge.core.context_aggregator.extractors;

import org.millerM907.edge.core.context_aggregator.extractors.impl.UserAgentExtractor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("UserAgentExtractor — unit tests")
class UserAgentExtractorTest {

    private final UserAgentExtractor extractor = new UserAgentExtractor();

    private static ServerHttpRequest reqWithUa(String ua) {
        ServerHttpRequest req = mock(ServerHttpRequest.class);
        HttpHeaders headers = new HttpHeaders();
        if (ua != null) headers.add("User-Agent", ua);
        when(req.getHeaders()).thenReturn(headers);
        return req;
    }

    @Nested
    @DisplayName("Given no User-Agent header")
    class GivenNoHeader {
        @Test
        @DisplayName("When extract is called then returns empty map")
        void givenNoHeader_whenExtract_thenEmptyMap() {
            ServerHttpRequest req = reqWithUa(null);
            Map<String, String> out = extractor.extract(req);
            assertThat(out).isEmpty();
        }
    }

    @Nested
    @DisplayName("Given a User-Agent header")
    class GivenHeaderPresent {
        @Test
        @DisplayName("When header contains value then it is returned as userAgent")
        void givenUa_whenExtract_thenReturned() {
            ServerHttpRequest req = reqWithUa("Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0");
            Map<String, String> out = extractor.extract(req);
            assertThat(out).containsEntry("userAgent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0");
        }

        @Test
        @DisplayName("When header is empty string then returns empty map")
        void givenEmptyUa_whenExtract_thenEmptyMap() {
            ServerHttpRequest req = reqWithUa("");
            Map<String, String> out = extractor.extract(req);
            assertThat(out).isEmpty();
        }

        @Test
        @DisplayName("When header is whitespace then returns empty map")
        void givenWhitespaceUa_whenExtract_thenEmptyMap() {
            ServerHttpRequest req = reqWithUa("   ");
            Map<String, String> out = extractor.extract(req);
            assertThat(out).isEmpty();
        }
    }
}
