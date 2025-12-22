package org.millerM907.landing.models;

import java.util.Map;

public record LandingView(String templateName, Map<String, Object> modelAttributes) {
}
