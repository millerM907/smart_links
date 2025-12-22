package org.millerM907.rules.dsl.conditions;

/**
 * Represents a node in the abstract syntax tree (AST) of a rule condition within the DSL.
 * <p>
 * A {@code CondDef} defines the logical structure of a condition that determines
 * whether a rule should be applied. It can represent:
 * <ul>
 *   <li>Logical composition of subconditions (e.g., {@link AllDef}, {@link AnyDef}, {@link NotDef})</li>
 *   <li>A single predicate comparison (e.g., {@link PredDef})</li>
 * </ul>
 * <p>
 * Each concrete subtype describes a specific logical operator:
 * <ul>
 *   <li>{@link AllDef} — all nested conditions must be true (logical AND)</li>
 *   <li>{@link AnyDef} — at least one nested condition must be true (logical OR)</li>
 *   <li>{@link NotDef} — negates a nested condition (logical NOT)</li>
 *   <li>{@link PredDef} — atomic predicate comparing an attribute with a value using an operator</li>
 * </ul>
 * <p>
 * This interface is <b>sealed</b> to restrict its implementations to a fixed,
 * well-defined set of condition types and ensure structural consistency in the DSL.
 *
 * @see AllDef
 * @see AnyDef
 * @see NotDef
 * @see PredDef
 */
public sealed interface CondDef permits AllDef, AnyDef, NotDef, PredDef {
}
