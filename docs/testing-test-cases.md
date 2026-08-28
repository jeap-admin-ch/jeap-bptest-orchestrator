# Testing custom test cases

The library ships dedicated test-support classes for unit-testing `TestCaseBaseInterface` implementations without a
Spring context.

## `TestCaseRunner`

`TestCaseRunner` simulates the orchestrator lifecycle:

- `run(testCase)` runs `prepare(...)` and `execute(...)` synchronously.
- `runAsync(testCase)` runs the same flow asynchronously and returns a `CompletableFuture<Void>`.
- `notify(notificationDto)` sends a synchronous `NotificationEvent` to the test case.
- `notifyAsync(notificationDto, delay, unit)` schedules an asynchronous notification; this is only allowed after
  `runAsync(...)`.
- `getApplicationEventPublisher()` returns the publisher instance the test case should use in the test.
- `getTestId()` and `getCallbackUrl()` expose the generated test id and callback URL.

When the runner receives an `ExecuteDoneEvent` with the matching test id and test-case name, it automatically calls
`verify(...)` and `cleanUp(...)`.

## `TestCaseMockTool`

`TestCaseMockTool` is built around Mockito mocks of `TestAgentWebClient` and `TestReportService`. It provides verified
helpers for common expectations:

- `mockPrepareCall(...)`
- `mockActCall(...)`
- `assertPrepareCalled(...)`
- `assertActCalled(...)` / `assertActsCalled(...)`
- `assertUpdateCalled(...)`
- `assertVerifyCalled(...)`
- `assertPersistTestResultCalled(...)`
- `assertDeleteCalled(...)`
- `assertNoMoreTestAgentWebClientInteractions()`
- `assertNoMoreTestReportServiceInteractions()`

## `TestCaseTestBase`

`TestCaseTestBase` is a small JUnit base class that creates fresh per-test fixtures:

- `testcaseRunner`
- `testId`
- `testAgentWebClientMock`
- `testReportServiceMock`
- `testRunServiceStub`
- `testCaseMockTool`

`TestRunServiceStub` stores parameters in memory and asserts that all calls use the active test id.

## Typical pattern

```java
class MyTestCaseTest extends TestCaseTestBase {

    @Test
    void happyPath() {
        MyTestCase testCase = new MyTestCase(
                testcaseRunner.getApplicationEventPublisher(),
                testAgentWebClientMock,
                testReportServiceMock,
                testRunServiceStub);

        testcaseRunner.run(testCase);

        testCaseMockTool.assertPrepareCalled("OrderTestAgent", testCase.getTestCaseName());
        testCaseMockTool.assertActCalled("OrderTestAgent", "submitOrder");

        testcaseRunner.notify(NotificationDto.builder()
                .testId(testId)
                .notification("Order-Closed")
                .producer("order-testagent")
                .build());

        testCaseMockTool.assertVerifyCalled("OrderTestAgent");
        testCaseMockTool.assertDeleteCalled("OrderTestAgent");
    }
}
```

See `TestCaseRunnerTest` in this repository for concrete patterns, including ordered asynchronous notifications and the
completion semantics of `runAsync(...)`.

## Related pages

- [Test-case lifecycle](test-case-lifecycle.md)
- [Getting started](getting-started.md)
