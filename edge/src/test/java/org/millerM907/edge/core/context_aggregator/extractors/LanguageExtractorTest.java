package org.millerM907.edge.core.context_aggregator.extractors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.millerM907.edge.core.context_aggregator.extractors.impl.LanguageExtractor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("LanguageExtractor — unit tests (given/when/then)")
class LanguageExtractorTest {

    private final LanguageExtractor extractor = new LanguageExtractor();

    private static ServerHttpRequest requestWithHeader(String value) {
        ServerHttpRequest req = mock(ServerHttpRequest.class, RETURNS_DEEP_STUBS);
        HttpHeaders headers = new HttpHeaders();
        if (value != null) {
            headers.add("Accept-Language", value);
        }
        when(req.getHeaders()).thenReturn(headers);
        return req;
    }

    @Nested
    @DisplayName("Given null or blank Accept-Language header")
    class GivenNullOrBlankHeader {

        @Test
        @DisplayName("When header is null then lang is UNKNOWN")
        void givenNullHeader_whenExtract_thenUnknown() {
            ServerHttpRequest req = requestWithHeader(null);

            Map<String, String> attrs = extractor.extract(req);

            assertThat(attrs)
                    .containsEntry("lang", "UNKNOWN");
        }

        @Test
        @DisplayName("When header is empty string then lang is UNKNOWN")
        void givenEmptyHeader_whenExtract_thenUnknown() {
            ServerHttpRequest req = requestWithHeader("");

            Map<String, String> attrs = extractor.extract(req);

            assertThat(attrs)
                    .containsEntry("lang", "UNKNOWN");
        }

        @Test
        @DisplayName("When header is only whitespace then lang is UNKNOWN")
        void givenWhitespaceHeader_whenExtract_thenUnknown() {
            ServerHttpRequest req = requestWithHeader("   ");

            Map<String, String> attrs = extractor.extract(req);

            assertThat(attrs)
                    .containsEntry("lang", "UNKNOWN");
        }

        @Test
        @DisplayName("When first part before comma is empty then lang is UNKNOWN")
        void givenEmptyFirstPart_whenExtract_thenUnknown() {
            ServerHttpRequest req = requestWithHeader(",fr;q=0.8");

            Map<String, String> attrs = extractor.extract(req);

            assertThat(attrs)
                    .containsEntry("lang", "UNKNOWN");
        }

        @Test
        @DisplayName("When first part is only quality parameter then lang is UNKNOWN")
        void givenOnlyQualityParam_whenExtract_thenUnknown() {
            ServerHttpRequest req = requestWithHeader(";q=0.5");

            Map<String, String> attrs = extractor.extract(req);

            assertThat(attrs)
                    .containsEntry("lang", "UNKNOWN");
        }
    }

    @Nested
    @DisplayName("Given simple language codes")
    class GivenSimpleCodes {

        @Test
        @DisplayName("When header is single language code then it is used as-is")
        void givenSingleCode_whenExtract_thenSameCodeReturned() {
            ServerHttpRequest req = requestWithHeader("en");

            Map<String, String> attrs = extractor.extract(req);

            assertThat(attrs)
                    .containsEntry("lang", "en");
        }

        @Test
        @DisplayName("When header contains locale with region then full code is returned")
        void givenLocaleWithRegion_whenExtract_thenFullCodeReturned() {
            ServerHttpRequest req = requestWithHeader("en-US");

            Map<String, String> attrs = extractor.extract(req);

            assertThat(attrs)
                    .containsEntry("lang", "en-US");
        }

        @Test
        @DisplayName("When header has leading and trailing spaces then code is trimmed")
        void givenCodeWithSpaces_whenExtract_thenTrimmed() {
            ServerHttpRequest req = requestWithHeader("  fr-FR  ");

            Map<String, String> attrs = extractor.extract(req);

            assertThat(attrs)
                    .containsEntry("lang", "fr-FR");
        }
    }

    @Nested
    @DisplayName("Given complex Accept-Language header")
    class GivenComplexHeader {

        @Test
        @DisplayName("When header contains multiple values with q then first language is selected")
        void givenMultipleValuesWithQ_whenExtract_thenFirstLanguageSelected() {
            ServerHttpRequest req = requestWithHeader("en-US,en;q=0.5");

            Map<String, String> attrs = extractor.extract(req);

            assertThat(attrs)
                    .containsEntry("lang", "en-US");
        }

        @Test
        @DisplayName("When header contains spaces and q params then first trimmed language is selected")
        void givenSpacesAndQParams_whenExtract_thenTrimmedLanguageSelected() {
            ServerHttpRequest req = requestWithHeader("  de-DE  , en-US;q=0.8, fr;q=0.7");

            Map<String, String> attrs = extractor.extract(req);

            assertThat(attrs)
                    .containsEntry("lang", "de-DE");
        }

        @Test
        @DisplayName("When first value has quality parameter then code before semicolon is used")
        void givenFirstWithQParam_whenExtract_thenCodeBeforeSemicolonUsed() {
            ServerHttpRequest req = requestWithHeader("de;q=0.7, en-US;q=0.9");

            Map<String, String> attrs = extractor.extract(req);

            assertThat(attrs)
                    .containsEntry("lang", "de");
        }
    }

    @Nested
    @DisplayName("Given integration behavior of extract method")
    class GivenExtractIntegration {

        @Test
        @DisplayName("When header is valid then map contains exactly one lang entry")
        void givenValidHeader_whenExtract_thenSingleLangKeyPresent() {
            ServerHttpRequest req = requestWithHeader("en-US,en;q=0.5");

            Map<String, String> attrs = extractor.extract(req);

            assertThat(attrs)
                    .hasSize(1)
                    .containsEntry("lang", "en-US");
        }

        @Test
        @DisplayName("When header is missing then map still contains lang with UNKNOWN")
        void givenMissingHeader_whenExtract_thenLangUnknownPresent() {
            ServerHttpRequest req = requestWithHeader(null);

            Map<String, String> attrs = extractor.extract(req);

            assertThat(attrs)
                    .hasSize(1)
                    .containsEntry("lang", "UNKNOWN");
        }
    }
}

