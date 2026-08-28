# Test-case lifecycle

Custom business-process flows are implemented as Spring beans that implement `TestCaseBaseInterface`.

## Contract

The current interface requires:

- `getTestCaseName()`
- `getJiraProjectKey()`
- `getZephyrTestCaseKey()`
- `prepare(String testId, PreparationDto preparationDto)`
- `execute(String testId)`
- `verify(String testId)`
- `cleanUp(String testId)`
- `onApplicationEvent(NotificationEvent event)` via `ApplicationListener<NotificationEvent>`

`execute(...)` is annotated with `@Async` on the interface, and async execution is enabled by the library's
`AsyncConfig`.

## Phase responsibilities

### Prepare

`TestCaseService.startTestRun(...)` creates a `PreparationDto` with:

- `callbackBaseUrl = orchestrator.callbackUrl`
- `testCase = getTestCaseName()`

Your implementation can add `data` entries and usually calls `TestAgentWebClient.prepare(...)` once per participating
test agent.

### Execute

This phase starts the active business-process interaction. Typical work includes calling one or more
`TestAgentWebClient.act(...)` operations and optionally storing or reading shared parameters through `TestRunService`.

### Notification-driven advancement

A running test case receives `NotificationEvent`s through Spring application events. This is the usual place to react
to asynchronous process milestones:

- update another test agent through `TestAgentWebClient.update(...)`
- store dynamic values in `TestRunService.setParameter(...)`
- publish `ExecuteDoneEvent` when execution is complete and verification may start

`NotificationEvent` exposes:

- `getNotification()`
- `getProducer()`
- `getTestId()`
- `getData()`

### Verify

In `verify(...)`, call `TestAgentWebClient.verify(...)` for the relevant test agents and persist each returned
`ReportDto` through `TestReportService.persistTestResult(...)`.

The library aggregates all `TestResult`s of all persisted reports belonging to the run into one `TestReport` entity.
Its overall conclusion is:

- `NO_RESULT` if there are no results
- `FAIL` if any result is `FAIL`
- `PASS` otherwise

### Clean up

Delete test data through `TestAgentWebClient.delete(...)`. When your cleanup is complete, publish
`TestRunFinishedEvent`; `TestCaseService` listens for that event and marks the `TestRun` as `ENDED`.

## Example pattern

```java
@Override
public void onApplicationEvent(NotificationEvent event) {
    switch (event.getNotification()) {
        case "Order-Created" -> testAgentWebClient.update(
                BILLING_TEST_AGENT,
                event.getTestId(),
                new DynamicDataDto(event.getData()));
        case "Order-Closed" -> applicationEventPublisher.publishEvent(
                new ExecuteDoneEvent(this, getTestCaseName(), event.getTestId()));
        default -> throw new IllegalStateException("Unexpected notification: " + event.getNotification());
    }
}
```

## Timeout and failure behaviour

When a run exceeds `orchestrator.testRunTimeout`, a timer task calls `TestRunService.abortLongRunningTestRun(...)`.
That method:

1. checks that the run is still `STARTED`
2. tries to call `verify(...)`
3. marks the run `ABORTED`
4. reports the failure to Zephyr
5. tries to call `cleanUp(...)`

The same abort path is used for `TestAgentException`s during prepare, execute, verify, or cleanup.

## Related pages

- [Architecture](architecture.md)
- [Testing custom test cases](testing-test-cases.md)
- [Zephyr reporting](reporting.md)
