# Metrics UI

The library includes a simple HTML metrics view backed by Thymeleaf.

## Endpoints

- `GET /metrics` renders the metrics table.
- `GET /` redirects to `/metrics`.

These mappings come from `MetricsViewController`. The library does not expose Prometheus metrics; it exposes an HTML
summary page.

## What is shown

`TestCaseMetricsService` loads all persisted `TestCase`s and aggregates their `TestRun`s into:

- total counted test runs
- successful test runs
- failed test runs
- average duration of successful runs
- average duration of failed runs

A `STARTED` run is ignored because it has not completed yet. An `ABORTED` run counts as failed. An `ENDED` run counts as
failed if its report conclusion is `FAIL` or `NO_RESULT`.

Durations are formatted as `HH:MM:SS.xx`.

## Intended use

This page is useful as a lightweight operational overview for an orchestrator instance. If you need machine-readable
metrics, you must add them in your consuming service; this library currently only provides the HTML view.

## Related pages

- [Architecture](architecture.md)
