# Smart Links — Rules Service

## Overview

The **Rules** service resolves a `slug` and a user **attribute map** into a target landing URL, based on a custom **YAML DSL**.

The Edge service calls `POST /resolve`, passing a `slug` plus request-derived attributes. Rules loads rule definitions from `rules.yaml`, compiles them into an executable representation, caches compiled rule sets by `(slug, configVersion)`, and then evaluates them to produce a `Resolution`.

## Architecture

![Rules Component Diagram](../docs/diagrams/rules-service.png)

## Core components

- **ResolveController** — `POST /resolve` REST controller; accepts a `ResolveRequest` and returns a `Resolution` as JSON.
- **ResolveService (ResolveUseCase)** — orchestrates rule set loading, compilation, versioned caching, and rule evaluation.
- **RuleSetRepository (InMemoryRuleSetRepository)** — in-memory storage for `RuleSet` objects by slug, with config version support.
- **DslLoader (ClasspathYamlLoader)** — loads and parses `rules.yaml` into the DSL model (`DslRoot`, `RuleSet`, `RuleDef`, `CondDef`, `ThenDef`).
- **RuleCompiler (DefaultRuleCompiler)** — compiles DSL conditions into executable predicates and actions.
- **OperatorRegistry & Operators (`eq`, `in`, `between`, `regex`)** — operator registry and implementations.
- **Accessor (DottedPathAccessor)** — reads values from the context using “dotted” paths (e.g., `browser`, `device`, `geo.country`).
- **RuleEvaluator (DefaultRuleEvaluator)** — evaluates compiled rules in priority order; returns the first match or the fallback (default) URL.
- **ErrorHandler** — maps domain exceptions to HTTP errors (`400` and `500`).

## Custom YAML DSL

Rules uses a custom **YAML DSL** so routing logic can be changed without modifying Java code.

Each `slug` entry in `rules.yaml` defines:

- `defaultUrl` — fallback URL if no rules match.
- `rules[]` — a list of rules:
  - `priority` — evaluation priority (higher wins / evaluated first).
  - `when` — a logical condition (`all`, `any`, `not`, predicates).
  - `then` — an action (`url`, optional `ttl`, optional `reason`).

Conditions are built from:

- logical combinators: `all`, `any`, `not`
- predicates: `{ path, operator, value }`
- operators: `eq`, `in`, `between` (time-based), `regex`

### DSL skeleton (illustrative)

```yaml
some-slug:
  defaultUrl: "https://example.com/default"
  rules:
    - priority: 100
      when:
        all:
          - path: "device"
            operator: "eq"
            value: "mobile"
          - path: "geo.country"
            operator: "in"
            value: ["NL", "BE"]
      then:
        url: "https://example.com/mobile-nl"
        ttl: 60
        reason: "Mobile users in NL/BE"
```

## Design notes (patterns & SOLID)

- **Composite / Specification for conditions**
  - `CondDef` hierarchy (`AllDef`, `AnyDef`, `NotDef`, `PredDef`) represents a logical condition tree.
  - `DefaultRuleCompiler` turns that tree into executable `Condition` instances.
  - Complex rules can be composed from simple ones without changing the core.

- **Strategy for operators**
  - `Operator` plus implementations (`EqOperator`, `InOperator`, `BetweenTimeOperator`, `RegexOperator`) encapsulate different comparison approaches.
  - `DefaultOperatorRegistry` receives operators via dependency injection and registers them by name.
  - Adding a new operator does not require changes to the compiler (**OCP**, **DIP**).

- **Repository for rule sets**
  - `RuleSetRepository` abstracts where configuration comes from.
  - `InMemoryRuleSetRepository` can be replaced with a DB-backed or config-service-backed implementation without rewriting `ResolveService`.

- **Factory-style compilation**
  - `RuleCompiler` acts like a factory: converts a `RuleSet` into an immutable `CompiledRuleSet` ready for evaluation.
  - Separating “definition” from “execution” improves testability and keeps responsibilities clear.

- **Versioned cache**
  - `ResolveService` caches compiled rule sets by `(slug, version)`.
  - When the configuration changes, the version changes and cached entries are naturally invalidated (**SRP**, **OCP**).

## Tech stack

- **Java 17**
- **Spring Boot 3 (Web)** — Spring MVC / REST
- **Spring Validation** — request DTO validation (when enabled)
- **SnakeYAML, Jackson Dataformat YAML** — YAML parsing and object mapping
- **Spring Boot Test, JUnit 5, Mockito** — controller and domain tests
- **JaCoCo** — test coverage analysis (threshold enforced via build config)
- **Maven** — build and dependency management

## Build and run

Requirements: **JDK 17** and **Maven**.

```bash
mvn clean package
mvn spring-boot:run
```