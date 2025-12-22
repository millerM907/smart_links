package org.millerM907.landing.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.millerM907.landing.config.LandingProperties;
import org.millerM907.landing.core.ConfigLandingPageResolver;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ConfigLandingPageResolver — unit tests (given/when/then)")
class ConfigLandingPageResolverTest {

    private ConfigLandingPageResolver createResolver(Map<String, String> mappings, String defaultTemplate) {
        LandingProperties landingProperties = new LandingProperties();
        landingProperties.setTemplateMappings(new HashMap<>(mappings));
        landingProperties.setDefaultTemplate(defaultTemplate);
        return new ConfigLandingPageResolver(landingProperties);
    }

    @Nested
    @DisplayName("Given a slug with explicit mapping")
    class GivenExplicitMapping {

        @Test
        @DisplayName("When resolveTemplateName is called then configured template is returned")
        void givenKnownSlug_whenResolveTemplateName_thenReturnsConfiguredTemplate() {
            Map<String, String> mappings = Map.of("desktop", "landing/desktop");
            ConfigLandingPageResolver resolver = createResolver(mappings, "landing/default");

            String templateName = resolver.resolveTemplateName("desktop");

            assertThat(templateName).isEqualTo("landing/desktop");
        }

        @Test
        @DisplayName("When configured template is blank then slug-based fallback is used")
        void givenBlankConfiguredTemplate_whenResolveTemplateName_thenSlugFallback() {
            Map<String, String> mappings = Map.of("promo", "   ");
            ConfigLandingPageResolver resolver = createResolver(mappings, "landing/default");

            String templateName = resolver.resolveTemplateName("promo");

            assertThat(templateName).isEqualTo("landing/promo");
        }
    }

    @Nested
    @DisplayName("Given a slug without explicit mapping")
    class GivenNoMapping {

        @Test
        @DisplayName("When resolveTemplateName is called then landing/slug is returned")
        void givenUnknownSlug_whenResolveTemplateName_thenLandingPrefixUsed() {
            Map<String, String> mappings = Map.of("desktop", "landing/desktop");
            ConfigLandingPageResolver resolver = createResolver(mappings, "landing/default");

            String templateName = resolver.resolveTemplateName("mobile");

            assertThat(templateName).isEqualTo("landing/mobile");
        }
    }

    @Nested
    @DisplayName("Given blank or null slug")
    class GivenBlankOrNullSlug {

        @Test
        @DisplayName("When slug is empty string then default template is returned")
        void givenEmptySlug_whenResolveTemplateName_thenDefaultTemplate() {
            ConfigLandingPageResolver resolver = createResolver(Map.of(), "landing/default");

            String templateName = resolver.resolveTemplateName("");

            assertThat(templateName).isEqualTo("landing/default");
        }

        @Test
        @DisplayName("When slug is whitespace then default template is returned")
        void givenWhitespaceSlug_whenResolveTemplateName_thenDefaultTemplate() {
            ConfigLandingPageResolver resolver = createResolver(Map.of(), "landing/default");

            String templateName = resolver.resolveTemplateName("   ");

            assertThat(templateName).isEqualTo("landing/default");
        }

        @Test
        @DisplayName("When slug is null then default template is returned")
        void givenNullSlug_whenResolveTemplateName_thenDefaultTemplate() {
            ConfigLandingPageResolver resolver = createResolver(Map.of(), "landing/default");

            String templateName = resolver.resolveTemplateName(null);

            assertThat(templateName).isEqualTo("landing/default");
        }
    }
}
