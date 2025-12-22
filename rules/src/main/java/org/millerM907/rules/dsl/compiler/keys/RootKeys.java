package org.millerM907.rules.dsl.compiler.keys;

public enum RootKeys {
    LINKS("links");
    private final String name;

    RootKeys(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
