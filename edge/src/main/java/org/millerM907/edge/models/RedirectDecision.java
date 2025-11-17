package org.millerM907.edge.models;

public record RedirectDecision(String targetUrl, boolean preserveMethod, int ttlSeconds, String reason) { }
