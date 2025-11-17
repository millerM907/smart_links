package org.millerM907.rules.dsl.compiler.keys;

public enum RuleKeys {
    ID("id"),
    PRIORITY("priority"),
    WHEN("when"),
    THEN("then");
    private final String name;

    RuleKeys(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
