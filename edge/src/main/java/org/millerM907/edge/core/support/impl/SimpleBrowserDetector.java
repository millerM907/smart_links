package org.millerM907.edge.core.support.impl;

import org.millerM907.edge.core.support.base.BrowserDetector;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class SimpleBrowserDetector implements BrowserDetector {

    @Override
    public String detect(String userAgent) {
        if (userAgent == null) return "Unknown";
        String s = userAgent.toLowerCase(Locale.ROOT);
        if (s.contains("firefox")) return "Firefox";
        if (s.contains("chrome")) return "Chrome";
        if (s.contains("safari")) return "Safari";
        return "Unknown";
    }
}
