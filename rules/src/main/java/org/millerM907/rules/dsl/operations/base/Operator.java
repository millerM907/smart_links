package org.millerM907.rules.dsl.operations.base;

/**
 * Defines a comparison or logical operator used in rule evaluation.
 * <p>
 * Each implementation provides a named operation (e.g. "eq", "in", "between")
 * and performs a comparison between actual and expected values.
 * Operators are stateless and registered in {@link OperatorRegistry}.
 * </p>
 */
public interface Operator {

    /**
     * Returns the unique name of the operator (e.g. "eq", "in").
     *
     * @return operator name
     */
    String name();

    /**
     * Tests whether the given actual value satisfies the operator's condition with the expected value.
     *
     * @param actual   runtime value from the context
     * @param expected expected value from the rule
     * @return true if the condition matches
     */
    boolean test(Object actual, Object expected);
}

