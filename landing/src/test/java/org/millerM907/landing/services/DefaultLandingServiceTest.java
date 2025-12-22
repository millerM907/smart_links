package org.millerM907.landing.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.millerM907.landing.core.LandingPageResolver;
import org.millerM907.landing.models.LandingView;
import org.millerM907.landing.services.DefaultLandingService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("DefaultLandingService — unit tests (given/when/then)")
class DefaultLandingServiceTest {

    private final LandingPageResolver landingPageResolver = mock(LandingPageResolver.class);
    private final DefaultLandingService landingService = new DefaultLandingService(landingPageResolver);

    @Nested
    @DisplayName("Given a non-null slug")
    class GivenNonNullSlug {

        @Test
        @DisplayName("When getLanding is called then view contains template from resolver and page in model")
        void givenSlug_whenGetLanding_thenUsesResolverAndSetsPageAttribute() {
            when(landingPageResolver.resolveTemplateName("desktop")).thenReturn("landing/desktop");

            LandingView landingView = landingService.getLanding("desktop");

            assertThat(landingView.templateName()).isEqualTo("landing/desktop");
            assertThat(landingView.modelAttributes()).containsEntry("page", "desktop");
            verify(landingPageResolver).resolveTemplateName(eq("desktop"));
        }
    }

    @Nested
    @DisplayName("Given a null slug")
    class GivenNullSlug {

        @Test
        @DisplayName("When getLanding is called then resolver is invoked and model contains null page")
        void givenNullSlug_whenGetLanding_thenResolverInvokedAndPageNull() {
            when(landingPageResolver.resolveTemplateName(null)).thenReturn("landing/default");

            LandingView landingView = landingService.getLanding(null);

            assertThat(landingView.templateName()).isEqualTo("landing/default");
            assertThat(landingView.modelAttributes()).containsEntry("page", null);
            verify(landingPageResolver).resolveTemplateName(null);
        }
    }
}
