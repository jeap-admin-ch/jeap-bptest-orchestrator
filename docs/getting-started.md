# Getting started

Business process tests sit at the top of the test pyramid. They validate that multiple applications work together across
system boundaries. They are not the right place to re-test every domain rule in detail; their focus is cross-application
interaction and overall process flow.

This repository provides the library support for building a business process test orchestrator. The usual setup is:

1. Create a Spring Boot application for your orchestrator service.
2. Use `jeap-bptest-orchestrator-instance` as the Maven parent.
3. Expose the orchestrator REST endpoints used to start test runs and receive test-agent callbacks.
4. Implement one or more `TestCaseBaseInterface` beans that define the process-specific lifecycle.
5. Configure PostgreSQL, the participating test agents, and Zephyr reporting.

## Maven parent

A downstream orchestrator service typically inherits from the POM-only `jeap-bptest-orchestrator-instance` module:

```xml
<parent>
  <groupId>ch.admin.bit.jeap</groupId>
  <artifactId>jeap-bptest-orchestrator-instance</artifactId>
  <version>use-the-current-version</version>
  <relativePath />
</parent>
```

That parent brings in the `jeap-bptest-orchestrator` library and relaxes javadoc and license-plugin behaviour for
service-instance projects.

## Minimal REST controller

The library does not ship its own public REST controller. A consuming service is expected to provide one that delegates
into the library services. The service methods and payload types below are verified against the current source code:

```java
package com.example.orchestrator.web;

import ch.admin.bit.jeap.testagent.api.notification.LogDto;
import ch.admin.bit.jeap.testagent.api.notification.NotificationDto;
import ch.admin.bit.jeap.testorchestrator.domain.TestConclusion;
import ch.admin.bit.jeap.testorchestrator.services.LogService;
import ch.admin.bit.jeap.testorchestrator.services.NotificationService;
import ch.admin.bit.jeap.testorchestrator.services.TestCaseService;
import ch.admin.bit.jeap.testorchestrator.services.TestRunService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tests")
class TestCaseController {

    private final TestCaseService testCaseService;
    private final LogService logService;
    private final NotificationService notificationService;
    private final TestRunService testRunService;

    @PostMapping("/{testCase}")
    String startTestRun(@PathVariable String testCase) {
        return testCaseService.startTestRun(testCase);
    }

    @PostMapping("/{testId}/logs")
    void log(@PathVariable String testId, @RequestBody LogDto logDto) {
        logService.log(testId, logDto);
    }

    @PostMapping("/{testId}/notifications")
    void notify(@PathVariable String testId, @RequestBody NotificationDto notificationDto) {
        notificationService.notify(testId, notificationDto);
    }

    @GetMapping("/{testId}/conclusion")
    TestConclusion getOverallTestConclusion(@PathVariable String testId) {
        return testRunService.getOverallTestConclusion(testId);
    }
}
```

The start endpoint should not wrap the whole request in a transaction. `TestCaseService.startTestRun(...)` persists the
`TestRun` before `prepare(...)` completes so that incoming log callbacks can already be stored.

## Minimal test case implementation

Each process-specific test case implements `TestCaseBaseInterface`. The exact contract is documented in
[Test-case lifecycle](test-case-lifecycle.md); the example below shows the verified interaction style with the current
API.

```java
package com.example.orchestrator.testcases;

import ch.admin.bit.jeap.testagent.api.act.ActionDto;
import ch.admin.bit.jeap.testagent.api.notification.NotificationDto;
import ch.admin.bit.jeap.testagent.api.prepare.PreparationDto;
import ch.admin.bit.jeap.testagent.api.update.DynamicDataDto;
import ch.admin.bit.jeap.testagent.api.verify.ReportDto;
import ch.admin.bit.jeap.testorchestrator.adapter.testagent.TestAgentWebClient;
import ch.admin.bit.jeap.testorchestrator.domain.events.ExecuteDoneEvent;
import ch.admin.bit.jeap.testorchestrator.domain.events.NotificationEvent;
import ch.admin.bit.jeap.testorchestrator.domain.events.TestRunFinishedEvent;
import ch.admin.bit.jeap.testorchestrator.services.TestCaseBaseInterface;
import ch.admin.bit.jeap.testorchestrator.services.TestReportService;
import ch.admin.bit.jeap.testorchestrator.services.TestRunService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class OrderBillingHappyPath implements TestCaseBaseInterface {

    private static final String ORDER_TEST_AGENT = "OrderTestAgent";
    private static final String BILLING_TEST_AGENT = "BillingTestAgent";

    private final ApplicationEventPublisher applicationEventPublisher;
    private final TestAgentWebClient testAgentWebClient;
    private final TestReportService testReportService;
    private final TestRunService testRunService;

    @Override
    public String getTestCaseName() {
        return "OrderBillingHappyPath";
    }

    @Override
    public String getJiraProjectKey() {
        return "JEAP";
    }

    @Override
    public String getZephyrTestCaseKey() {
        return "JEAP-T16";
    }

    @Override
    public void prepare(String testId, PreparationDto preparationDto) {
        preparationDto.setData(java.util.Map.of("demoKey", "demoValue"));
        testAgentWebClient.prepare(ORDER_TEST_AGENT, testId, preparationDto);
        testAgentWebClient.prepare(BILLING_TEST_AGENT, testId, preparationDto);
    }

    @Override
    public void execute(String testId) {
        testAgentWebClient.act(ORDER_TEST_AGENT, testId, ActionDto.builder().action("submitOrder").build());
        testRunService.getParameters(testId);
    }

    @Override
    public void verify(String testId) {
        ReportDto billingReport = testAgentWebClient.verify(BILLING_TEST_AGENT, testId);
        testReportService.persistTestResult(testId, billingReport);
        ReportDto orderReport = testAgentWebClient.verify(ORDER_TEST_AGENT, testId);
        testReportService.persistTestResult(testId, orderReport);
    }

    @Override
    public void cleanUp(String testId) {
        testAgentWebClient.delete(BILLING_TEST_AGENT, testId);
        testAgentWebClient.delete(ORDER_TEST_AGENT, testId);
        applicationEventPublisher.publishEvent(new TestRunFinishedEvent(this, testId));
    }

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
}
```

## Runtime requirements

The library expects a relational database with JPA and Flyway. Its default JPA properties and shipped Flyway migrations
are PostgreSQL-oriented, and the runtime dependencies include `flyway-database-postgresql` plus the PostgreSQL JDBC
driver. H2 is used only in tests.

## Next steps

- [Architecture](architecture.md)
- [Configuration reference](configuration.md)
- [Test-case lifecycle](test-case-lifecycle.md)
- [Testing custom test cases](testing-test-cases.md)
