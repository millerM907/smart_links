package org.millerM907.landing.controllers;

import org.millerM907.landing.services.LandingService;
import org.millerM907.landing.models.LandingView;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class LandingController {

    private final LandingService landingService;

    public LandingController(LandingService landingService) {
        this.landingService = landingService;
    }

    @GetMapping("/landing/{slug}")
    public String page(@PathVariable("slug") String slug, Model model) {
        LandingView landingView = landingService.getLanding(slug);
        model.addAllAttributes(landingView.modelAttributes());
        return landingView.templateName();
    }
}
