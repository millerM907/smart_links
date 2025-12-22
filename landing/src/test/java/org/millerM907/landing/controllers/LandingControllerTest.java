package org.millerM907.landing.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.millerM907.landing.models.LandingView;
import org.millerM907.landing.services.LandingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = LandingController.class)
@DisplayName("LandingController — MVC tests")
class LandingControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    LandingService landingService;

    private LandingView landingView(String template, Map<String, Object> model) {
        return new LandingView(template, model);
    }

    @Nested
    @DisplayName("Given a valid slug")
    class GivenValidSlug {

        @Test
        @DisplayName("When GET /landing/{slug} then returns view name and model attributes")
        void givenValidSlug_whenGetLanding_thenReturnsViewAndModel() throws Exception {
            String slug = "ru-desktop-firefox";
            LandingView landingView = landingView(
                    "landing/ru-desktop-firefox",
                    Map.of("title", "RU Firefox Desktop", "segment", "test-segment")
            );
            when(landingService.getLanding(eq(slug))).thenReturn(landingView);

            mockMvc.perform(get("/landing/{slug}", slug))
                    .andExpect(status().isOk())
                    .andExpect(view().name("landing/ru-desktop-firefox"))
                    .andExpect(model().attribute("title", "RU Firefox Desktop"))
                    .andExpect(model().attribute("segment", "test-segment"));

            verify(landingService).getLanding(eq(slug));
        }
    }

    @Nested
    @DisplayName("Given empty model from service")
    class GivenEmptyModel {

        @Test
        @DisplayName("When service returns landing with empty model then request succeeds with that view")
        void givenEmptyModel_whenGetLanding_thenOkWithViewAndNoExtraModel() throws Exception {
            String slug = "desktop";
            LandingView landingView = landingView("landing/desktop", Map.of());
            when(landingService.getLanding(eq(slug))).thenReturn(landingView);

            var result = mockMvc.perform(get("/landing/{slug}", slug))
                    .andExpect(status().isOk())
                    .andExpect(view().name("landing/desktop"))
                    .andReturn();

            Map<String, Object> modelMap = result.getModelAndView().getModel();
            assertThat(modelMap).doesNotContainKey("title");
            verify(landingService).getLanding(eq(slug));
        }
    }

    @Nested
    @DisplayName("Given service failure")
    class GivenServiceFailure {

        @Test
        @DisplayName("When service throws then server error is returned")
        void givenServiceThrows_whenGetLanding_thenServerError() throws Exception {
            String slug = "broken";
            when(landingService.getLanding(eq(slug)))
                    .thenThrow(new IllegalStateException("No landing mapping"));

            mockMvc.perform(get("/landing/{slug}", slug))
                    .andExpect(status().is5xxServerError());
        }
    }
}
