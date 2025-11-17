package org.millerM907.landing.core;

import org.millerM907.landing.config.LandingProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ConfigLandingPageResolver implements LandingPageResolver {

    private final LandingProperties landingProperties;

    public ConfigLandingPageResolver(LandingProperties landingProperties) {
        this.landingProperties = landingProperties;
    }

    @Override
    public String resolveTemplateName(String slug) {
        String normalizedSlug = (slug == null) ? "" : slug.trim();
        Map<String, String> templateMappings = landingProperties.getTemplateMappings();

        String configuredTemplate = templateMappings.get(normalizedSlug);
        if (configuredTemplate != null && !configuredTemplate.isBlank()) {
            return configuredTemplate;
        }

        if (!normalizedSlug.isEmpty()) {
            return "landing/" + normalizedSlug;
        }

        return landingProperties.getDefaultTemplate();
    }
}
