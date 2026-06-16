package ch.admin.bit.jeap.testorchestrator.metrics;

import ch.admin.bit.jeap.testorchestrator.adapter.jpa.TestCaseJpaRepository;
import ch.admin.bit.jeap.testorchestrator.adapter.jpa.TestRunJpaRepository;
import ch.admin.bit.jeap.testorchestrator.domain.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestCaseMetricsServiceTest {

    private static final String TEST_RESULT_NAME = "testResultName";
    private static final String TEST_RESULT_DETAIL = "TestResultDetail";
    private static final String ENV = "env";

    @InjectMocks
    private TestCaseMetricsService testCaseMetricsService;

    @Mock
    private TestCaseJpaRepository testCaseJpaRepository;

    @Mock
    private TestRunJpaRepository testRunJpaRepository;

    @Test
    void getTestCaseWithSomeTestRuns() {
        TestCase testCase = new TestCase("TEST_CASE", "jiraKey", "zephyrKey");

        // TestRun 1:
        // - SUCCESS
        // - Dauer: 30 Sek
        TestRun testRun1 = new TestRun(ENV, testCase);
        TestReport testReport1 = new TestReport(UUID.randomUUID(), "testReport_1");
        TestResult testResult11 = new TestResult(TEST_RESULT_NAME, TEST_RESULT_DETAIL, TestConclusion.PASS);
        TestResult testResult12 = new TestResult(TEST_RESULT_NAME, TEST_RESULT_DETAIL, TestConclusion.PASS);
        testReport1.add(testResult11);
        testReport1.add(testResult12);
        testRun1.setTestReport(testReport1);
        testRun1.setEndedAt(testRun1.getStartedAt().plusSeconds(30));
        testRun1.setTestState(TestState.ENDED);

        // TestRun 2:
        // - FAILED
        // - Dauer: 2 Minuten
        TestRun testRun2 = new TestRun(ENV, testCase);
        TestReport testReport2 = new TestReport(UUID.randomUUID(), "testReport_2");
        TestResult testResult21 = new TestResult(TEST_RESULT_NAME, TEST_RESULT_DETAIL, TestConclusion.FAIL);
        TestResult testResult22 = new TestResult(TEST_RESULT_NAME, TEST_RESULT_DETAIL, TestConclusion.PASS);
        testReport2.add(testResult21);
        testReport2.add(testResult22);
        testRun2.setTestReport(testReport2);
        testRun2.setEndedAt(testRun2.getStartedAt().plusMinutes(2));
        testRun2.setTestState(TestState.ENDED);

        // TestRun 3:
        // - FAILED
        // - Dauer: 15 Minuten
        TestRun testRun3 = new TestRun(ENV, testCase);
        TestReport testReport3 = new TestReport(UUID.randomUUID(), "testReport_3");
        TestResult testResult31 = new TestResult(TEST_RESULT_NAME, TEST_RESULT_DETAIL, TestConclusion.FAIL);
        testReport3.add(testResult31);
        testRun3.setTestReport(testReport3);
        testRun3.setEndedAt(testRun3.getStartedAt().plusMinutes(15));
        testRun3.setTestState(TestState.ENDED);

        // TestRun 4:
        // - PASS
        // - Dauer: 23 Minuten
        TestRun testRun4 = new TestRun(ENV, testCase);
        TestReport testReport4 = new TestReport(UUID.randomUUID(), "testReport_4");
        TestResult testResult41 = new TestResult(TEST_RESULT_NAME, TEST_RESULT_DETAIL, TestConclusion.PASS);
        testReport4.add(testResult41);
        testRun4.setTestReport(testReport4);
        testRun4.setEndedAt(testRun4.getStartedAt().plusMinutes(24));
        testRun4.setTestState(TestState.ENDED);

        when(testCaseJpaRepository.findAll()).thenReturn(List.of(testCase));
        when(testRunJpaRepository.findByTestCase(testCase)).thenReturn(List.of(testRun1, testRun2, testRun3, testRun4));

        List<TestCaseMetricsDto> metricsDtoList = testCaseMetricsService.getAllTestCaseMetrics();
        assertNotNull(metricsDtoList);
        assertEquals(1, metricsDtoList.size());

        assertEquals("TEST_CASE", metricsDtoList.get(0).testCaseName());
        assertEquals(4, metricsDtoList.get(0).totalTestRuns());
        assertEquals(2, metricsDtoList.get(0).failedTestRuns());
        assertEquals(2, metricsDtoList.get(0).successfulTestRuns());
        assertEquals("00:08:30.00", metricsDtoList.get(0).averageFailedDuration());
        assertEquals("00:12:15.00", metricsDtoList.get(0).averageSuccessDuration());
    }

    @Test
    void getTestCasesWithNoTestRuns() {
        TestCase testCase1 = new TestCase("TEST_CASE_1", "jiraKey", "zephyrKey");
        TestCase testCase2 = new TestCase("TEST_CASE_2", "jiraKey", "zephyrKey");

        when(testCaseJpaRepository.findAll()).thenReturn(List.of(testCase1, testCase2));

        List<TestCaseMetricsDto> metricsDtoList = testCaseMetricsService.getAllTestCaseMetrics();
        assertNotNull(metricsDtoList);
        assertEquals(2, metricsDtoList.size());
        assertEquals("TEST_CASE_1", metricsDtoList.get(0).testCaseName());
        assertEquals(0, metricsDtoList.get(0).totalTestRuns());
        assertEquals(0, metricsDtoList.get(0).failedTestRuns());
        assertEquals(0, metricsDtoList.get(0).successfulTestRuns());
        assertEquals("TEST_CASE_2", metricsDtoList.get(1).testCaseName());
        assertEquals(0, metricsDtoList.get(1).totalTestRuns());
        assertEquals(0, metricsDtoList.get(1).failedTestRuns());
        assertEquals(0, metricsDtoList.get(1).successfulTestRuns());
    }

    @Test
    void getTestCaseWithSomeAbortedTestRuns() {
        TestCase testCase = new TestCase("TEST_CASE", "jiraKey", "zephyrKey");

        // TestRun 1: is just STARTED, but nothing more
        // --> Should not count in the Overall TestRuns
        TestRun testRun1 = new TestRun(ENV, testCase);
        testRun1.setTestState(TestState.STARTED);

        // TestRun 2: is ENDED, but has a TestResult which FAILED
        // --> FAILED TESTRUN
        // --> COUNT AS TESTRUN
        TestRun testRun2 = new TestRun(ENV, testCase);
        testRun2.setTestState(TestState.ENDED);
        TestReport testReport2 = new TestReport(UUID.randomUUID(), "testReport_2");
        TestResult testResult21 = new TestResult(TEST_RESULT_NAME, TEST_RESULT_DETAIL, TestConclusion.FAIL);
        TestResult testResult22 = new TestResult(TEST_RESULT_NAME, TEST_RESULT_DETAIL, TestConclusion.PASS);
        testReport2.add(testResult21);
        testReport2.add(testResult22);
        testRun2.setTestReport(testReport2);
        testRun2.setEndedAt(testRun2.getStartedAt().plusMinutes(10));

        // TestRun 3: is ABORTED, but TestCases are Passed
        // This can happen, when JIRA ist not reachable, but all TestAgent are responding correctly
        // --> FAILED TESTRUN
        // --> COUNT AS TESTRUN
        TestRun testRun3 = new TestRun(ENV, testCase);
        testRun3.setTestState(TestState.ABORTED);
        TestReport testReport3 = new TestReport(UUID.randomUUID(), "testReport_3");
        TestResult testResult31 = new TestResult(TEST_RESULT_NAME, TEST_RESULT_DETAIL, TestConclusion.PASS);
        testReport3.add(testResult31);
        testRun3.setTestReport(testReport3);
        testRun3.setEndedAt(testRun3.getStartedAt().plusMinutes(15));

        // TestRun 4: is ABORTED, but no TestResults are available
        // This can happen, when a TestAgent is not reachable
        // --> FAILED TESTRUN
        // --> COUNT AS TESTRUN
        TestRun testRun4 = new TestRun(ENV, testCase);
        testRun4.setTestState(TestState.ABORTED);


        when(testCaseJpaRepository.findAll()).thenReturn(List.of(testCase));
        when(testRunJpaRepository.findByTestCase(testCase)).thenReturn(List.of(testRun1, testRun2, testRun3, testRun4));

        List<TestCaseMetricsDto> metricsDtoList = testCaseMetricsService.getAllTestCaseMetrics();
        assertNotNull(metricsDtoList);
        assertEquals(1, metricsDtoList.size());

        assertEquals("TEST_CASE", metricsDtoList.get(0).testCaseName());
        assertEquals(3, metricsDtoList.get(0).totalTestRuns());
        assertEquals(3, metricsDtoList.get(0).failedTestRuns());
        assertEquals(0, metricsDtoList.get(0).successfulTestRuns());

    }
}