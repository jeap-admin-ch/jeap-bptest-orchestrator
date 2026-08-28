# jEAP Business Process Test Orchestrator

jEAP Business Process Test Orchestrator is a library for orchestrating end-to-end business process tests across multiple applications. It provides the domain model, persistence, test-agent client, test-case lifecycle, Zephyr reporting integration, and test-support utilities needed to build an orchestrator service on top of Spring Boot.

Most consumers do not run this repository directly. Instead, they create their own orchestrator service, inherit from `jeap-bptest-orchestrator-instance`, implement one or more `TestCaseBaseInterface` test cases, and expose the small REST API that test agents use for logs and notifications.

## Documentation

Start with [Getting started](docs/getting-started.md), then follow the links below.

| Topic | File |
|---|---|
| Getting started | [docs/getting-started.md](docs/getting-started.md) |
| Architecture | [docs/architecture.md](docs/architecture.md) |
| Configuration reference | [docs/configuration.md](docs/configuration.md) |
| Test-case lifecycle | [docs/test-case-lifecycle.md](docs/test-case-lifecycle.md) |
| TestAgent API contract | [docs/test-agent-api.md](docs/test-agent-api.md) |
| Testing custom test cases | [docs/testing-test-cases.md](docs/testing-test-cases.md) |
| Zephyr reporting | [docs/reporting.md](docs/reporting.md) |
| Metrics UI | [docs/metrics.md](docs/metrics.md) |

## Changes

This library is versioned using [Semantic Versioning](http://semver.org/) and all changes are documented in
[CHANGELOG.md](./CHANGELOG.md) following the format defined in [Keep a Changelog](http://keepachangelog.com/).

## Note

This repository is part the open source distribution of jEAP. See [github.com/jeap-admin-ch/jeap](https://github.com/jeap-admin-ch/jeap)
for more information.

## License

This repository is Open Source Software licensed under the [Apache License 2.0](./LICENSE).
