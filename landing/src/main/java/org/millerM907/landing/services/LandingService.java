package org.millerM907.landing.services;

import org.millerM907.landing.models.LandingView;

/**
 * Application service for building landing page views.
 */
public interface LandingService {

    /**
     * Builds a landing page view descriptor for the given slug.
     *
     * @param slug last path segment from {@code /landing/{slug}}, may be {@code null}
     * @return view descriptor containing template name and model attributes
     */
    LandingView getLanding(String slug);
}