package org.millerM907.landing.core;

/**
 * Resolves a view template for a landing page slug.
 */
public interface LandingPageResolver {

    /**
     * Returns a view template name for the given slug.
     *
     * @param slug last path segment from {@code /landing/{slug}}, may be {@code null}
     * @return template name to render, never {@code null}
     */
    String resolveTemplateName(String slug);
}
