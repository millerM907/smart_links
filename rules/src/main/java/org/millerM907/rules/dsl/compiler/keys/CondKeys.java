package org.millerM907.rules.dsl.compiler.keys;

public enum CondKeys {
    ALL("all"),
    ANY("any"),
    NOT("not"),
    PATH("path"),
    OPERATOR("operator"),
    VALUE("value");
    private final String name;

    CondKeys(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
