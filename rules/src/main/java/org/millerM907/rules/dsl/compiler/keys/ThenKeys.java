package org.millerM907.rules.dsl.compiler.keys;

public enum ThenKeys {
    URL("url"),
    TTL("ttl"),
    REASON("reason");
    private final String name;

    ThenKeys(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
