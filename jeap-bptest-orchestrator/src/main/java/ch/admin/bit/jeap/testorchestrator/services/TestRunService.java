package ch.admin.bit.jeap.testorchestrator.services;

import ch.admin.bit.jeap.testorchestrator.adapter.jpa.TestRunJpaRepository;
import ch.admin.bit.jeap.testorchestrator.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestRunService {

    @Setter
    protected Map<String, TestCaseBaseInterface> testCasesMap;
    private final TestReportService testReportService;

    private final TestRunJpaRepository testRunJpaRepository;
    private final PlatformTransactionManager transactionManager;

    @Value("${orchestrator.testRunTimeout:30000}")
    private long testRunTimeout;

    public String getParameterValue(String testId, String key) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        return transactionTemplate.execute(transactionStatus -> {
            TestRun testRun = testRunJpaRepository.getReferenceById(UUID.fromString(testId));
            return testRun.getParameters().get(key);
        });
    }

    public Map<String, String> getParameters(String testId) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        return transactionTemplate.execute(transactionStatus -> {
            TestRun testRun = testRunJpaRepository.getReferenceById(UUID.fromString(testId));
            return Map.copyOf(testRun.getParameters());
        });
    }

    public void setParameter(String testId, String key, String value) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.executeWithoutResult(status -> {
            TestRun testRun = testRunJpaRepository.getReferenceById(UUID.fromString(testId));
            testRun.getParameters().put(key, value);
            testRunJpaRepository.save(testRun);
        });
    }

    /**
     * Ends a TestRun means: Set EndDate and the State to 'ENDED'
     */
    public void endTestRun(String testId) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.executeWithoutResult(status -> {
            TestRun testRun = testRunJpaRepository.getReferenceById(UUID.fromString(testId));
            testRun.setEndedAt(ZonedDateTime.now());
            testRun.setTestState(TestState.ENDED);
            testRunJpaRepository.save(testRun);
        });
    }

    /**
     * Aborts a TestRun, when the TestAgents acting weird (no connection, timeout, ..)
     * Means: Set EndDate and set the State to 'ABORTED'
     */
    public void abortTestRun(String testId, String message) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.executeWithoutResult(status -> {

            TestRun testRun = testRunJpaRepository.getReferenceById(UUID.fromString(testId));
            TestCaseBaseInterface testInstance = getTestInstance(testRun.getTestCase().getName());
            try {
                testInstance.verify(testId);
            } catch (Exception e) {
                log.error("TestRun {} wasn't verified correctly due to error {}", testId, e);
            }

            TestReport testReport = testRun.getTestReport();
            if (testReport != null) {
                List<TestResult> testResults = testReport.getTestResults();
                String testResultDetail = "TestId: %s; Test was aborted. Reason: %s,\n %s ".format(testId, message, testResults.get(0).getDetail());
                testReport.remove(0);
                testReport.setDetail(testResultDetail);
            } else {
                testReport = new TestReport(UUID.fromString(testId),
                        "TestId: " + testId + "; Test was aborted. Reason: " + message);
            }
            testRun.setEndedAt(ZonedDateTime.now());
            testRun.setTestState(TestState.ABORTED);
            testRun.setTestReport(testReport);
            testRunJpaRepository.save(testRun);
            try {
                testReportService.reportToJira(testId);
                testInstance.cleanUp(testId);
            } catch (Exception e) {
                log.error("TestRun {} wasn't cleaned up correctly due to error {}", testId, e);
            }
        });
    }

    /**
     * Aborts a TestRun, when the orchestrator.testRunTimeout is reached and the TestRun is
     * in State 'STARTED'
     */
    public void abortLongRunningTestRun(String testId) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.executeWithoutResult(status -> {
            TestRun testRun = testRunJpaRepository.getReferenceById(UUID.fromString(testId));
            TestState testState = testRun.getTestState();

            if (testState.equals(TestState.STARTED)) {
                TestCaseBaseInterface testInstance = getTestInstance(testRun.getTestCase().getName());
                try {
                    testInstance.verify(testId);
                } catch (Exception e) {
                    log.error("TestRun {} wasn't verified correctly due to error {}", testId, e);
                }
                TestReport testReport = testRun.getTestReport();
                if (testReport != null) {
                    List<TestResult> testResults = testReport.getTestResults();
                    String testResultDetail = "TestId: %s; The test lasted too long (>%s,\n %s seconds) --&gt; Aborted".format(testId, testRunTimeout / 1000, testResults.get(0).getDetail());
                    testReport.remove(0);
                    testReport.setDetail(testResultDetail);
                } else {
                    testReport = new TestReport(UUID.fromString(testId),
                            "TestId: " + testId + "; The test lasted too long (>" + testRunTimeout / 1000 + " seconds) --&gt; Aborted");
                }
                testRun.setEndedAt(ZonedDateTime.now());
                testRun.setTestState(TestState.ABORTED);
                testRun.setTestReport(testReport);
                testRunJpaRepository.save(testRun);
                try {
                    testReportService.reportToJira(testId);
                    testInstance.cleanUp(testId);
                } catch (Exception e) {
                    log.error("TestRun {} wasn't cleaned up correctly due to error {}", testId, e);
                }
            }
        });
    }

    /**
     * Returns True if the TestRun has the State Started, otherwise FALSE
     */
    public boolean isTestRunInProgress(String testId) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        return Boolean.TRUE.equals(transactionTemplate.execute(transactionStatus -> {
            TestRun testRun = testRunJpaRepository.getReferenceById(UUID.fromString(testId));
            return TestState.STARTED.equals(testRun.getTestState());
        }));
    }

    public TestConclusion getOverallTestConclusion(String testId) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        return transactionTemplate.execute(transactionStatus -> {
            TestRun testRun = testRunJpaRepository.getReferenceById(UUID.fromString(testId));
            if (testRun.getTestReport() == null) {
                return TestConclusion.NO_RESULT;
            }
            return testRun.getTestReport().getOverallTestConclusion();
        });
    }

    TestRun createTestRun(String zephyrEnvironment, TestCase actualTestCase) {
        TestRun newTestRun = new TestRun(zephyrEnvironment, actualTestCase);
        return testRunJpaRepository.save(newTestRun);
    }

    public TestCaseBaseInterface getTestInstance(String testCaseName) {
        return testCasesMap.get(testCaseName);
    }
}
