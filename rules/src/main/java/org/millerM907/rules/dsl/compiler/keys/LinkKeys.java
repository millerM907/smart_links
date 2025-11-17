package org.millerM907.rules.dsl.compiler.keys;

public enum LinkKeys {
    SLUG("slug"),
    DEFAULT_URL("defaultUrl"),
    RULES("rules");
    private final String name;

    LinkKeys(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
