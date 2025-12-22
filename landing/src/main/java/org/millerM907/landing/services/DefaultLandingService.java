package org.millerM907.landing.services;

import org.millerM907.landing.core.LandingPageResolver;
import org.millerM907.landing.models.LandingView;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DefaultLandingService implements LandingService {

    private final LandingPageResolver landingPageResolver;

    public DefaultLandingService(LandingPageResolver landingPageResolver) {
        this.landingPageResolver = landingPageResolver;
    }

    @Override
    public LandingView getLanding(String slug) {
        String templateName = landingPageResolver.resolveTemplateName(slug);

        Map<String, Object> modelAttributes = new HashMap<>();
        modelAttributes.put("page", slug);

        return new LandingView(templateName, modelAttributes);
    }
}
