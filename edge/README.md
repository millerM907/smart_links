# Smart Links — Edge Redirector Service

## Overview

The **Edge** service is the entry point of the Smart Links system. It handles incoming requests to `/s/{slug}`, builds a **request context** from HTTP headers and query parameters, computes a **fingerprint** for caching, and resolves the final destination URL via the **Rules** service.

High-level flow:

1. Client requests `GET /s/{slug}`.
2. Edge builds `RequestContext` (headers, `X-Demo-*` headers, query params), computes a fingerprint, and checks the in-memory cache.
3. On a cache miss, Edge calls the Rules service (`POST /resolve`) with the slug and context attributes.
4. Edge caches the result and returns an HTTP redirect to the resolved URL (typically a Landing page).

## Architecture

![Edge Component Diagram](../docs/diagrams/edge-service.png)

## Key components

- **RedirectController** — HTTP entry point for `/s/{slug}`; delegates to the service layer and returns the redirect response.
- **DefaultRedirectService** — orchestrates request context aggregation, fingerprint calculation, cache lookup, and the Rules service call.
- **RequestContextAggregator** — assembles `RequestContext` using a chain of extractors.
- **AttributeExtractor*** — small, extensible components that extract individual attributes (browser, device, time, language, etc.) from headers and query parameters.
- **FingerprintStrategy** — converts `slug + context` into a stable cache key.
- **ResolutionCache (Caffeine)** — in-memory cache for resolved mappings (`slug + context → targetUrl`).
- **RuleEnginePort / RuleEngineHttpClient** — a port abstraction and an HTTP adapter used to call the Rules service.

## Design notes (patterns & SOLID)

- **Strategy**
  - `FingerprintStrategy`, `BrowserDetector`, and `AttributeExtractor` provide interchangeable implementations.
  - Interfaces + dependency injection keep the design aligned with **DIP** and **OCP**.

- **Pipeline / Chain of Responsibility**
  - `RequestContextAggregator` applies a list of `AttributeExtractor` beans sequentially.
  - Adding a new attribute is typically done by adding a new extractor (no changes to existing ones), supporting **SRP** and **OCP**.

- **Ports & Adapters**
  - `RuleEnginePort` defines the integration boundary; `RuleEngineHttpClient` is an adapter on top of `WebClient`.
  - Business logic depends on an abstraction, not HTTP details (**DIP**).

- **Cache abstraction**
  - `ResolutionCache` encapsulates caching behavior and the underlying provider (Caffeine).
  - Caching can be replaced or disabled without rewriting core logic.

## Tech stack

- **Java 17**
- **Spring Boot 3 (WebFlux)**
- **Project Reactor (Mono/Flux)**
- **Caffeine** (in-memory cache)
- **JUnit 5 / Mockito / Spring Boot Test**
- **JaCoCo** (coverage)
- **Maven**

## Build and run

Requirements: **JDK 17** and **Maven**.

```bash
mvn clean package
mvn spring-boot:run
```

By default, Edge is expected to run on port `8080`.

## Testing with synthetic headers (X-Demo-*)

For demos and local testing, Edge supports synthetic `X-Demo-*` headers. They override values that would otherwise be derived from standard headers and server time, and they become part of the `RequestContext`.

### Supported headers

- `X-Demo-Browser` → `browser`  
  Example values: `Chrome`, `Firefox`, `Safari`

- `X-Demo-Device` → `device`  
  Example values: `desktop`, `mobile`, `tablet`  
  Default: `desktop`

- `X-Demo-Time` → `time` (user local time)  
  Format: `HH:mm` (e.g., `10:30`, `21:05`)  
  Default: server current time

- `X-Demo-Tz` → `tz` (timezone)  
  Example values: `UTC`, `Europe/Amsterdam`, `Asia/Kolkata`  
  Default: `UTC`

- `X-Demo-Geo` → `country` (or a geo label)  
  Example values: `NL`, `RU`, `US` (or any region string)  
  Default: `UNKNOWN`

### Extra attributes via query parameters

All query parameters with the `attrs.*` prefix are extracted into standalone attributes **without** the prefix:

`?attrs.segment=premium&attrs.campaign=BLACKFRIDAY` → `segment=premium`, `campaign=BLACKFRIDAY`

### Example request (curl)

```bash
curl -v "http://localhost:8080/s/sale1111" \
  -H "X-Demo-Browser: Chrome" \
  -H "X-Demo-Device: desktop" \
  -H "X-Demo-Time: 10:00" \
  -H "X-Demo-Tz: Asia/Kolkata" \
  -H "X-Demo-Geo: IN"
```