package ch.admin.bit.jeap.testorchestrator.services;

import ch.admin.bit.jeap.testagent.api.prepare.PreparationDto;
import ch.admin.bit.jeap.testorchestrator.adapter.jpa.TestCaseJpaRepository;
import ch.admin.bit.jeap.testorchestrator.adapter.testagent.TestAgentException;
import ch.admin.bit.jeap.testorchestrator.domain.TestCase;
import ch.admin.bit.jeap.testorchestrator.domain.TestRun;
import ch.admin.bit.jeap.testorchestrator.domain.events.ExecuteDoneEvent;
import ch.admin.bit.jeap.testorchestrator.domain.events.TestRunFinishedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.logstash.logback.argument.StructuredArguments.kv;
import static net.logstash.logback.argument.StructuredArguments.value;

@Service
@Slf4j
public class TestCaseService {

    private final Map<String, TestCaseBaseInterface> testCasesMap;
    private final Map<String, Timer> timerMap;
    private final TestReportService testReportService;
    private final TestRunService testRunService;
    private final TestCaseJpaRepository testCaseJpaRepository;
    private final String callbackUrl;
    private final String zephyrEnvironment;
    private final long testRunTimeout;

    /**
     * This Service injects the specific TestCase-Implementation by naming Convention.
     * It starts the TestRun, and act's as EventListener (Spring Application Events)
     */
    public TestCaseService(List<TestCaseBaseInterface> testCases,
                           TestReportService testReportService,
                           TestRunService testRunService,
                           TestCaseJpaRepository testCaseJpaRepository,
                           @Value("${orchestrator.callbackUrl}") String callbackUrl,
                           @Value("${orchestrator.zephyr.zephyrEnvironment}") String zephyrEnvironment,
                           @Value("${orchestrator.testRunTimeout:30000}") long testRunTimeout) {
        this.testCasesMap = testCases.stream()
                .collect(Collectors.toMap(TestCaseBaseInterface::getTestCaseName, Function.identity()));
        this.testReportService = testReportService;
        this.testRunService = testRunService;
        this.testCaseJpaRepository = testCaseJpaRepository;
        this.callbackUrl = callbackUrl;
        this.zephyrEnvironment = zephyrEnvironment;
        this.testRunTimeout = testRunTimeout;
        this.timerMap = new HashMap<>();
        //Inject testCaseMap with Setter. Injection via Constructor leads to cycle dependency
        testRunService.setTestCasesMap(this.testCasesMap);
    }

    /**
     * 1. Creates a new TestRun and if there is no TestCase, a new one will be created.
     * 2. Call 'prepare' on the specific TestCase Implementation
     * 3. Call 'execute' on the specific TestCase Implementation
     *
     * @param testCaseName The testCaseName must be the same as the Name of specific class, which implements the
     *                     TestCaseBaseInterface.
     */
    public String startTestRun(String testCaseName) {
        // 1. We get a TestCaseInstance from the injected classes based on the TestCaseName
        TestCaseBaseInterface testCaseInstance = testCasesMap.get(testCaseName);
        if (testCaseInstance == null) {
            throw new IllegalArgumentException("The TestCaseName '" + testCaseName +
                    "' does not fit with an implemented class name");
        }
        // 2. Create a TestRun and a TestCase (if necessary)
        TestRun testRun = createTestRun(testCaseName, testCaseInstance);
        String testId = testRun.getTestId();
        // 2.1 Start a Timer
        startTimer(testId, testCaseInstance);
        // 3. The Preparation needs at least the Information about the CallbackURL and TestCase
        PreparationDto preparationDto = PreparationDto.builder()
                .callbackBaseUrl(callbackUrl)
                .testCase(testCaseName)
                .build();
        // 4. Call prepare on the TestCase Implementation (synchronous)
        try {
            testCaseInstance.prepare(testId, preparationDto);
            log.info("Prepared {}", kv("testId", testRun.getTestId()));
            log.info("testcasename: {}", testRun.getTestCase().getName());
            log.info("testId: {}", testRun.getTestId());

            // 5. After prepare, call the Async-Method for execution
            testCaseInstance.execute(testId);
        } catch (TestAgentException testAgentException) {
            logTestAgentException(testAgentException);
            this.testRunService.abortTestRun(testId, testAgentException.getMessage());
        }
        return testId;
    }

    /**
     * Listen to the ExecuteDoneEvent and go on with the TestRun
     * - Call 'verify' on the specific TestCase Implementation and store the Report
     * - Call 'cleanUp' on the specific TestCase Implementation
     *
     * @param event ExecuteDoneEvent
     */
    @EventListener
    public void onApplicationEvent(ExecuteDoneEvent event) {
        TestCaseBaseInterface testCaseInstance = testCasesMap.get(event.getTestCaseName());
        String testId = event.getTestId();

        try {
            stopTimer(testId);

            testCaseInstance.verify(testId);
            log.info("Verified {}", kv("testId", testId));

            testReportService.reportToJira(testId);
            log.info("Reported {}", kv("testId", testId));

            testCaseInstance.cleanUp(testId);
            log.info("Cleaned {}", kv("testId", testId));

        } catch (TestAgentException testAgentException) {
            logTestAgentException(testAgentException);
            this.testRunService.abortTestRun(testId, testAgentException.getMessage());
        }
    }

    private void logTestAgentException(TestAgentException testAgentException) {
        log.error("[{}] Failed or timed out rest call for '{}'. Abort test run '{}'",
                value("testAgentName", testAgentException.getTestAgentName()),
                value("requestUrl", testAgentException.getRequestUrl()),
                value("testId", testAgentException.getTestId()));
    }

    /**
     * Listen to the TestRunFinishedEvent and ends the TestRun
     *
     * @param event TestRunFinishedEvent
     */
    @EventListener
    public void onApplicationTestRunFinished(TestRunFinishedEvent event) {
        testRunService.endTestRun(event.getTestId());
    }

    boolean isTimerRunning(String testId) {
       return timerMap.containsKey(testId);
    }

    /**
     * Creates a TestRun. If the Parent TestCase does not exist, creates a TestCase as well
     *
     * @param testCaseName          Name of the TestCase from this TestRun
     * @param testCaseBaseInterface Name of the Implementation-Class from this TestCase
     * @return A TestRun-Entity
     */
    private TestRun createTestRun(String testCaseName, TestCaseBaseInterface testCaseBaseInterface) {
        Optional<TestCase> testCaseOptional = testCaseJpaRepository.findByName(testCaseName);
        TestCase actualTestCase = testCaseOptional.orElseGet(() -> createTestCase(testCaseName, testCaseBaseInterface));
        return testRunService.createTestRun(zephyrEnvironment, actualTestCase);
    }

    /**
     * Creates a TestCase with the Name, and the JIRA-Zepyhr information from the TestCase-Implementations-Class
     *
     * @param testCaseName          Name of the TestCase
     * @param testCaseBaseInterface TestCase-Implementations-Class
     * @return A TestCase-Entity
     */
    private TestCase createTestCase(String testCaseName, TestCaseBaseInterface testCaseBaseInterface) {
        TestCase newTestCase = new TestCase(
                testCaseName,
                testCaseBaseInterface.getJiraProjectKey(),
                testCaseBaseInterface.getZephyrTestCaseKey());
        return testCaseJpaRepository.save(newTestCase);
    }

    private void startTimer(String testId, TestCaseBaseInterface testCaseInstance) {
        log.debug("Start timer {} for testcase", testId, testCaseInstance.getTestCaseName());
        TimerTask task = new TestRunTimerTask(testId, testRunService);
        Timer timer = new Timer();
        timer.schedule(task, testRunTimeout);
        timerMap.put(testId, timer);
    }

    private void stopTimer(String testId) {
        var timer = timerMap.remove(testId);
        if (timer != null) {
            log.debug("Stop timer {}", testId);
            timer.cancel();
            timer.purge();
        } else {
            log.debug("unexpected: no timer {} to stop", testId);
        }
    }

    /**
     * Inner Class: When the Timer is reached, the Testrun will be aborted, the Jira Zephry Report will be sent
     * and CleanUp will be trigger.
     * But only if the TestRun is in State 'STARTED'
     */
    @RequiredArgsConstructor
    static class TestRunTimerTask extends TimerTask {

        private final String testId;
        private final TestRunService testRunService;

        @Override
        public void run() {
            log.info("+++ TestRunTimerTask performed: {}", testId);
            if (this.testRunService.isTestRunInProgress(testId)) {
                log.info("Abort the TestRun and Report to JIRA Zephyr");
                this.testRunService.abortLongRunningTestRun(testId);
            } else {
                log.info("TestRun has already ended. No Report to JIRA");
            }
        }
    }

}
