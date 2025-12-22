package org.millerM907.landing.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration properties for landing pages.
 */
@ConfigurationProperties(prefix = "landing")
public class LandingProperties {

    /**
     * Fallback view template when no mapping is found.
     */
    private String defaultTemplate = "landing/default";

    /**
     * Mapping from slug to view template name.
     */
    private Map<String, String> templateMappings = new HashMap<>();

    public String getDefaultTemplate() {
        return defaultTemplate;
    }

    public void setDefaultTemplate(String defaultTemplate) {
        this.defaultTemplate = defaultTemplate;
    }

    public Map<String, String> getTemplateMappings() {
        return templateMappings;
    }

    public void setTemplateMappings(Map<String, String> templateMappings) {
        this.templateMappings = templateMappings;
    }
}
