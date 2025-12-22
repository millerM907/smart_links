# Smart Links — Landing Service

## Overview

The **Landing** service is responsible for serving HTML pages for resolved links.

After the **Edge** redirector and the **Rules** service determine the final destination, the user is redirected to
`/landing/{slug}`. The controller delegates to the service layer, which resolves the template name and prepares a view
model. Spring MVC’s view engine then renders the final HTML page.

## Architecture

![Landing Component Diagram](../docs/diagrams/landing-service.png)

## Key components

- **LandingController** — handles `GET /landing/{slug}`, calls `LandingService`, populates the Spring `Model`, and returns
  the view (template) name.
- **LandingService / DefaultLandingService** — business logic for selecting a template and preparing a `LandingView`
  (template name + model attributes).
- **LandingPageResolver / ConfigLandingPageResolver** — resolves a slug into a concrete template, with a fallback to the
  default template.
- **LandingProperties** — binds configuration from `application.yml` (map: `slug → template` + default template) into a
  strongly-typed Java configuration class.
- **LandingView** — a DTO containing `templateName` and a set of model attributes.

## Design notes (patterns & SOLID)

- **Strategy for landing selection**
  - `LandingPageResolver` is an interface; `ConfigLandingPageResolver` is the configuration-based implementation.
  - For more advanced scenarios (A/B testing, personalization), you can provide a different resolver implementation
    without changing the controller or service (**DIP**, **OCP**).

- **Service + ViewModel (`LandingView`)**
  - `DefaultLandingService` determines which template to use and which data to pass to the view.
  - `LandingView` acts as a view model: the controller only copies attributes into the Spring `Model` and returns the
    view name.
  - This keeps responsibilities clear (**SRP**) and makes unit testing easier.

- **Configuration as data**
  - `LandingProperties` loads the `slug → template` mapping from `application.yml`.
  - To add a new landing page, you typically only need to:
    1. create an HTML template under `templates/landing/`,
    2. add a `slug → template` mapping in the configuration.
  - Behavior is extended by data, not code changes (**OCP**).

## Tech stack

- **Java 17**
- **Spring Boot 3 (Web)** — Spring MVC and server-side templating
- **Spring Boot configuration properties** — binds `application.yml` into `LandingProperties`
- **Spring Boot Test, JUnit 5, Mockito** — unit testing
- **JaCoCo** — test coverage reports
- **Maven** — build and dependency management

## Build and run

Requirements: **JDK 17** and **Maven**.

```bash
mvn clean package
mvn spring-boot:run
```