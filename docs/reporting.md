# Zephyr reporting

`TestReportService` is responsible for turning the stored orchestrator report into a Zephyr test-run payload and posting
it through `ZephyrWebClient`.

## How reporting works

1. `verify(...)` in your test case fetches one or more `ReportDto`s from test agents.
2. `TestReportService.persistTestResult(...)` creates or updates the `TestReport` entity for the `TestRun`.
3. When `ExecuteDoneEvent` is handled, `TestCaseService` calls `testReportService.reportToJira(testId)`.
4. `ZephyrWebClient` posts a `ZephyrTestRunDto` to `orchestrator.zephyr.restApiUrl + orchestrator.zephyr.testRunPath`.

## Payload mapping

The current implementation maps orchestrator data as follows:

- Zephyr test-run name: `BusinessProcess Test Cycle`
- project key: `TestCase.getJiraProjectKey()`
- test-case key: `TestCase.getZepyhrTestCaseKey()` from the persisted `TestCase`
- environment: `TestRun.getEnvironment()`
- overall status: `Pass` for `TestConclusion.PASS`, otherwise `Fail`
- script steps: one `ZephyrStepDto` per persisted `TestResult`, using the result detail as the step comment

`TestConclusion.NO_RESULT` is therefore reported as `Fail` at the Zephyr level.

## Authentication

The current `ZephyrWebClient` uses HTTP Basic authentication via `orchestrator.zephyr.username` and
`orchestrator.zephyr.password`.

## Failure path

Abort paths also try to report the test run. `TestRunService.abortTestRun(...)` and
`abortLongRunningTestRun(...)` both end by calling `reportToJira(testId)` and then `cleanUp(...)`.

## Related pages

- [Configuration reference](configuration.md)
- [Test-case lifecycle](test-case-lifecycle.md)
