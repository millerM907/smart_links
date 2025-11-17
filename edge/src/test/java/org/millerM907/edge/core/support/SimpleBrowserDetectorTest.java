package org.millerM907.edge.core.support;

import org.millerM907.edge.core.support.impl.SimpleBrowserDetector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SimpleBrowserDetector — unit tests")
class SimpleBrowserDetectorTest {

    private final SimpleBrowserDetector detector = new SimpleBrowserDetector();

    @Nested
    @DisplayName("Given null or blank user agent")
    class GivenNullOrBlankUa {

        @Test
        @DisplayName("When UA is null then returns Unknown")
        void givenNull_whenDetect_thenUnknown() {
            assertThat(detector.detect(null)).isEqualTo("Unknown");
        }

        @Test
        @DisplayName("When UA is empty string then returns Unknown")
        void givenEmpty_whenDetect_thenUnknown() {
            assertThat(detector.detect("")).isEqualTo("Unknown");
        }

        @Test
        @DisplayName("When UA is whitespace then returns Unknown")
        void givenWhitespace_whenDetect_thenUnknown() {
            assertThat(detector.detect("   ")).isEqualTo("Unknown");
        }
    }

    @Nested
    @DisplayName("Given typical desktop user agents")
    class GivenTypicalDesktopUa {

        @Test
        @DisplayName("When UA contains 'Firefox' then returns Firefox")
        void givenFirefoxUa_whenDetect_thenFirefox() {
            String ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:125.0) Gecko/20100101 Firefox/125.0";
            assertThat(detector.detect(ua)).isEqualTo("Firefox");
        }

        @Test
        @DisplayName("When UA contains 'Chrome' then returns Chrome")
        void givenChromeUa_whenDetect_thenChrome() {
            String ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/124.0.0.0 Safari/537.36";
            assertThat(detector.detect(ua)).isEqualTo("Chrome");
        }

        @Test
        @DisplayName("When UA contains 'Safari' and not 'Chrome' then returns Safari")
        void givenSafariUa_whenDetect_thenSafari() {
            String ua = "Mozilla/5.0 (Macintosh; Intel Mac OS X 14_5) AppleWebKit/605.1.15 Version/17.5 Safari/605.1.15";
            assertThat(detector.detect(ua)).isEqualTo("Safari");
        }
    }

    @Nested
    @DisplayName("Given ambiguous or Chromium-based user agents")
    class GivenAmbiguousUa {

        @Test
        @DisplayName("When UA is Edge-like (contains Chrome token) then returns Chrome")
        void givenEdgeUa_whenDetect_thenChrome() {
            String ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/124.0.0.0 Safari/537.36 Edg/124.0.2478.51";
            assertThat(detector.detect(ua)).isEqualTo("Chrome");
        }

        @Test
        @DisplayName("When UA is iOS Chrome (CriOS without 'Chrome' token) then returns Safari")
        void givenIosChromeCriOS_whenDetect_thenSafari() {
            String ua = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_5 like Mac OS X) AppleWebKit/605.1.15 CriOS/124.0.0.0 Mobile/15E148 Safari/604.1";
            assertThat(detector.detect(ua)).isEqualTo("Safari");
        }
    }

    @Nested
    @DisplayName("Given case-insensitive inputs")
    class GivenCaseInsensitivity {

        @Test
        @DisplayName("When UA contains 'FIREFOX' uppercase then returns Firefox")
        void givenUppercaseFirefox_whenDetect_thenFirefox() {
            String ua = "SOMETHING FIREFOX SOMETHING".toUpperCase(Locale.ROOT);
            assertThat(detector.detect(ua)).isEqualTo("Firefox");
        }

        @Test
        @DisplayName("When UA contains 'chrome' lowercase then returns Chrome")
        void givenLowercaseChrome_whenDetect_thenChrome() {
            assertThat(detector.detect("chrome something")).isEqualTo("Chrome");
        }

        @Test
        @DisplayName("When UA contains 'sAfArI' mixed case then returns Safari")
        void givenMixedCaseSafari_whenDetect_thenSafari() {
            assertThat(detector.detect("xxx sAfArI yyy")).isEqualTo("Safari");
        }
    }

    @Nested
    @DisplayName("Given unrecognized user agents")
    class GivenUnrecognizedUa {

        @Test
        @DisplayName("When UA has no known tokens then returns Unknown")
        void givenUnknownUa_whenDetect_thenUnknown() {
            String ua = "CustomBot/1.0 (+https://example.org/bot)";
            assertThat(detector.detect(ua)).isEqualTo("Unknown");
        }
    }
}
