# AGENTS.md

Guidance for AI coding agents working **in this repository**. For how to *use* the library in a
consuming service, read [README.md](README.md) and the [docs/](docs/) folder instead.

## Project

jEAP Business Process Test Orchestrator is a multi-module Maven library for building orchestrator
services that run business process tests across several applications. The main library module provides
JPA persistence, the test-run state machine, the TestAgent REST client, Zephyr reporting integration,
a simple metrics UI, and unit-test helpers for custom test-case implementations. Downstream projects
usually inherit from `jeap-bptest-orchestrator-instance` and add their own Spring Boot application,
REST controller, and `TestCaseBaseInterface` implementations.

## Repository layout

```text
pom.xml                                  # Parent POM (packaging=pom); declares the modules below
jeap-bptest-orchestrator/                # Main library module
  src/main/java/ch/admin/bit/jeap/testorchestrator/
    domain/                              # JPA domain model: TestCase, TestRun, TestReport, TestResult, TestLog
    domain/events/                       # Spring application events driving the lifecycle
    services/                            # TestCaseService, TestRunService, reporting/logging/notification services
    adapter/jpa/                         # Spring Data JPA repositories and JPA auto-configuration
    adapter/testagent/                   # TestAgent REST client, timeout config, async exception handling
    adapter/zephyr/                      # Zephyr REST client, DTOs, configuration binding
    metrics/                             # Metrics HTML view and aggregation service
    testsupport/                         # TestCaseRunner, TestCaseMockTool, TestCaseTestBase
  src/main/resources/
    db/migration/                        # Flyway schema migrations
    templates/metrics.html               # Thymeleaf metrics page
    META-INF/spring/                     # AutoConfiguration.imports for the library
jeap-bptest-orchestrator-instance/       # POM-only parent for downstream orchestrator service instances
Jenkinsfile, publiccode.yml, CHANGELOG.md, LICENSE, setPomVersions.sh
```

## Build & test

```bash
./mvnw -pl jeap-bptest-orchestrator -am install    # build the main module and its dependencies
./mvnw verify                                      # full build incl. tests
./mvnw -pl jeap-bptest-orchestrator test           # main-module tests only
```

- Parent: `ch.admin.bit.jeap:jeap-spring-boot-parent`.
- The Jenkins pipeline runs integration and quality checks on feature and master branches; releases publish with the
  `maven-central-publish` profile.
- Persistence tests use the Spring Boot JPA test support with H2 plus Flyway migrations; web-client tests use WireMock.

## jEAP conventions

- Java packages live under `ch.admin.bit.jeap.testorchestrator...`.
- Configuration properties use the prefix `orchestrator.*`; Zephyr settings live under `orchestrator.zephyr.*`.
- Downstream services add custom business-process flows by implementing `TestCaseBaseInterface`; the bean name lookup is
  by `getTestCaseName()`, not by a separate registry.
- Test-case implementations usually react to `NotificationEvent` and publish `ExecuteDoneEvent` / `TestRunFinishedEvent`
  through Spring's `ApplicationEventPublisher` to advance the lifecycle.
- Prefer the provided test-support classes (`TestCaseRunner`, `TestCaseMockTool`, `TestCaseTestBase`) for unit tests of
  custom test cases instead of bootstrapping a full Spring context.

## Docs

When changing public behaviour, update the matching focused file under [docs/](docs/) (one topic per file) and
the documentation index in the README.

- Pages must be valid MDX (Docusaurus renders every `.md` as MDX) and any Mermaid diagrams must use correct
  Mermaid syntax — see the [writing principles](https://github.com/jeap-admin-ch/jeap/blob/master/docs/documenting-jeap.md#writing-principles).
- There is no standalone linter for this; validate by actually building the docs site locally against this
  checkout, using the [site repository](https://github.com/jeap-admin-ch/jeap-admin-ch.github.io)'s
  `preview.sh --local <path-to-this-repo> --no-autodiscover` (production build, catches MDX/Mermaid syntax errors
  and broken links) or `dev.sh` for a faster hot-reload check.

## Versioning

- Semantic Versioning; all changes documented in [CHANGELOG.md](./CHANGELOG.md) (Keep a Changelog format).
- `setPomVersions.sh` updates the version across all module POMs.
- When working on a feature branch, increase the version to `x.y.z-SNAPSHOT` in the POMs.
- Always keep the -SNAPSHOT postfix in the POMs, CI will remove it when releasing a version. Do not use the
  SNAPSHOT postfix in other places (CHANGELOG, publiccode.yml etc.)
- Keep changelog entries concise and to the point, follow existing patterns.
- Keep commit messages short, use the JIRA ID from the branch name as a prefix, do not use conventional commits
  (for example: "JEAP-1234 Added feature X").
- When bumping the version, also update the changelog, and update version/date in `publiccode.yml`.
- When the version on a feature branch has not yet been bumped compared to master, ask the user if a major, minor
  or patch version bump should be performed, and update the version accordingly.
