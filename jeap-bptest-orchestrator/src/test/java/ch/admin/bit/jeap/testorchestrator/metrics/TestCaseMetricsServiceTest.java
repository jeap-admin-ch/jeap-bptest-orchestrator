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

    @InjectMocks
    private TestCaseMetricsService testCaseMetricsService;

    @Mock
    private TestCaseJpaRepository testCaseJpaRepository;

    @Mock
    private TestRunJpaRepository testRunJpaRepository;

    @Test
    void getTestCase_with_some_TestRuns() {
        TestCase testCase = new TestCase("TEST_CASE", "jiraKey", "zephyrKey");

        // TestRun 1:
        // - SUCCESS
        // - Dauer: 30 Sek
        TestRun testRun_1 = new TestRun("env", testCase);
        TestReport testReport_1 = new TestReport(UUID.randomUUID(), "testReport_1");
        TestResult testResult_1_1 = new TestResult("testResultName", "TestResultDetail", TestConclusion.PASS);
        TestResult testResult_1_2 = new TestResult("testResultName", "TestResultDetail", TestConclusion.PASS);
        testReport_1.add(testResult_1_1);
        testReport_1.add(testResult_1_2);
        testRun_1.setTestReport(testReport_1);
        testRun_1.setEndedAt(testRun_1.getStartedAt().plusSeconds(30));
        testRun_1.setTestState(TestState.ENDED);

        // TestRun 2:
        // - FAILED
        // - Dauer: 2 Minuten
        TestRun testRun_2 = new TestRun("env", testCase);
        TestReport testReport_2 = new TestReport(UUID.randomUUID(), "testReport_2");
        TestResult testResult_2_1 = new TestResult("testResultName", "TestResultDetail", TestConclusion.FAIL);
        TestResult testResult_2_2 = new TestResult("testResultName", "TestResultDetail", TestConclusion.PASS);
        testReport_2.add(testResult_2_1);
        testReport_2.add(testResult_2_2);
        testRun_2.setTestReport(testReport_2);
        testRun_2.setEndedAt(testRun_2.getStartedAt().plusMinutes(2));
        testRun_2.setTestState(TestState.ENDED);

        // TestRun 3:
        // - FAILED
        // - Dauer: 15 Minuten
        TestRun testRun_3 = new TestRun("env", testCase);
        TestReport testReport_3 = new TestReport(UUID.randomUUID(), "testReport_3");
        TestResult testResult_3_1 = new TestResult("testResultName", "TestResultDetail", TestConclusion.FAIL);
        testReport_3.add(testResult_3_1);
        testRun_3.setTestReport(testReport_3);
        testRun_3.setEndedAt(testRun_3.getStartedAt().plusMinutes(15));
        testRun_3.setTestState(TestState.ENDED);

        // TestRun 4:
        // - PASS
        // - Dauer: 23 Minuten
        TestRun testRun_4 = new TestRun("env", testCase);
        TestReport testReport_4 = new TestReport(UUID.randomUUID(), "testReport_4");
        TestResult testResult_4_1 = new TestResult("testResultName", "TestResultDetail", TestConclusion.PASS);
        testReport_4.add(testResult_4_1);
        testRun_4.setTestReport(testReport_4);
        testRun_4.setEndedAt(testRun_4.getStartedAt().plusMinutes(24));
        testRun_4.setTestState(TestState.ENDED);

        when(testCaseJpaRepository.findAll()).thenReturn(List.of(testCase));
        when(testRunJpaRepository.findByTestCase(testCase)).thenReturn(List.of(testRun_1, testRun_2, testRun_3, testRun_4));

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
    void getTestCases_with_no_TestRuns() {
        TestCase testCase_1 = new TestCase("TEST_CASE_1", "jiraKey", "zephyrKey");
        TestCase testCase_2 = new TestCase("TEST_CASE_2", "jiraKey", "zephyrKey");

        when(testCaseJpaRepository.findAll()).thenReturn(List.of(testCase_1, testCase_2));

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
    void getTestCase_with_some_TestRuns_Aborted() {
        TestCase testCase = new TestCase("TEST_CASE", "jiraKey", "zephyrKey");

        // TestRun 1: is just STARTED, but nothing more
        // --> Should not count in the Overall TestRuns
        TestRun testRun_1 = new TestRun("env", testCase);
        testRun_1.setTestState(TestState.STARTED);

        // TestRun 2: is ENDED, but has a TestResult which FAILED
        // --> FAILED TESTRUN
        // --> COUNT AS TESTRUN
        TestRun testRun_2 = new TestRun("env", testCase);
        testRun_2.setTestState(TestState.ENDED);
        TestReport testReport_2 = new TestReport(UUID.randomUUID(), "testReport_2");
        TestResult testResult_2_1 = new TestResult("testResultName", "TestResultDetail", TestConclusion.FAIL);
        TestResult testResult_2_2 = new TestResult("testResultName", "TestResultDetail", TestConclusion.PASS);
        testReport_2.add(testResult_2_1);
        testReport_2.add(testResult_2_2);
        testRun_2.setTestReport(testReport_2);
        testRun_2.setEndedAt(testRun_2.getStartedAt().plusMinutes(10));

        // TestRun 3: is ABORTED, but TestCases are Passed
        // This can happen, when JIRA ist not reachable, but all TestAgent are responding correctly
        // --> FAILED TESTRUN
        // --> COUNT AS TESTRUN
        TestRun testRun_3 = new TestRun("env", testCase);
        testRun_3.setTestState(TestState.ABORTED);
        TestReport testReport_3 = new TestReport(UUID.randomUUID(), "testReport_3");
        TestResult testResult_3_1 = new TestResult("testResultName", "TestResultDetail", TestConclusion.PASS);
        testReport_3.add(testResult_3_1);
        testRun_3.setTestReport(testReport_3);
        testRun_3.setEndedAt(testRun_3.getStartedAt().plusMinutes(15));

        // TestRun 4: is ABORTED, but no TestResults are available
        // This can happen, when a TestAgent is not reachable
        // --> FAILED TESTRUN
        // --> COUNT AS TESTRUN
        TestRun testRun_4 = new TestRun("env", testCase);
        testRun_4.setTestState(TestState.ABORTED);


        when(testCaseJpaRepository.findAll()).thenReturn(List.of(testCase));
        when(testRunJpaRepository.findByTestCase(testCase)).thenReturn(List.of(testRun_1, testRun_2, testRun_3, testRun_4));

        List<TestCaseMetricsDto> metricsDtoList = testCaseMetricsService.getAllTestCaseMetrics();
        assertNotNull(metricsDtoList);
        assertEquals(1, metricsDtoList.size());

        assertEquals("TEST_CASE", metricsDtoList.get(0).testCaseName());
        assertEquals(3, metricsDtoList.get(0).totalTestRuns());
        assertEquals(3, metricsDtoList.get(0).failedTestRuns());
        assertEquals(0, metricsDtoList.get(0).successfulTestRuns());

    }
}