package org.millerM907.edge.core.context_aggregator.extractors;

import org.millerM907.edge.core.context_aggregator.extractors.impl.DeviceExtractor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("DeviceExtractor — unit tests")
class DeviceExtractorTest {

    private final DeviceExtractor extractor = new DeviceExtractor();

    private static ServerHttpRequest mockRequestWithHeader(String headerValue) {
        ServerHttpRequest req = mock(ServerHttpRequest.class);
        HttpHeaders headers = new HttpHeaders();
        if (headerValue != null) {
            headers.add("X-Demo-Device", headerValue);
        }
        when(req.getHeaders()).thenReturn(headers);
        return req;
    }

    @Nested
    @DisplayName("Given no X-Demo-Device header")
    class GivenNoHeader {

        @Test
        @DisplayName("When extract is called then returns device=desktop")
        void givenNoHeader_whenExtract_thenDesktop() {
            ServerHttpRequest req = mockRequestWithHeader(null);

            Map<String, String> out = extractor.extract(req);

            assertThat(out).containsEntry("device", "desktop");
        }
    }

    @Nested
    @DisplayName("Given blank X-Demo-Device header")
    class GivenBlankHeader {

        @Test
        @DisplayName("When header is empty string then returns device=desktop")
        void givenEmptyString_whenExtract_thenDesktop() {
            ServerHttpRequest req = mockRequestWithHeader("");

            Map<String, String> out = extractor.extract(req);

            assertThat(out).containsEntry("device", "desktop");
        }

        @Test
        @DisplayName("When header is whitespace then returns device=desktop")
        void givenWhitespace_whenExtract_thenDesktop() {
            ServerHttpRequest req = mockRequestWithHeader("   ");

            Map<String, String> out = extractor.extract(req);

            assertThat(out).containsEntry("device", "desktop");
        }
    }

    @Nested
    @DisplayName("Given non-blank X-Demo-Device header")
    class GivenNonBlankHeader {

        @Test
        @DisplayName("When header is 'mobile' then returns device=mobile")
        void givenMobile_whenExtract_thenMobile() {
            ServerHttpRequest req = mockRequestWithHeader("mobile");

            Map<String, String> out = extractor.extract(req);

            assertThat(out).containsEntry("device", "mobile");
        }

        @Test
        @DisplayName("When header has surrounding spaces then value is preserved as-is")
        void givenSpacedValue_whenExtract_thenPreserved() {
            ServerHttpRequest req = mockRequestWithHeader(" mobile ");

            Map<String, String> out = extractor.extract(req);

            assertThat(out).containsEntry("device", " mobile ");
        }
    }
}
