package ch.admin.bit.jeap.testorchestrator.domain;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TestReportTest {

    public static final String TEST_REPORT_DETAIL = "Some Detail Information";

    @Test
    void creatingTestReportWithNoResult() {
        TestReport testReport = new TestReport(UUID.randomUUID(), TEST_REPORT_DETAIL);

        assertNotNull(testReport);
        assertNotNull(testReport.getId());
        assertEquals(TEST_REPORT_DETAIL, testReport.getDetail());
        assertEquals(0, testReport.getTestResults().size());
        assertEquals(TestConclusion.NO_RESULT, testReport.getOverallTestConclusion());
    }

    @Test
    void creatingTestReportWithAllFailedResult() {
        TestReport testReport = new TestReport(UUID.randomUUID(), TEST_REPORT_DETAIL);

        TestResult testResult_1 = new TestResult("TR1", TEST_REPORT_DETAIL, TestConclusion.FAIL);
        TestResult testResult_2 = new TestResult("TR2", TEST_REPORT_DETAIL, TestConclusion.FAIL);

        testReport.add(testResult_1);
        testReport.add(testResult_2);

        assertNotNull(testReport);
        assertNotNull(testReport.getId());
        assertEquals(2, testReport.getTestResults().size());
        assertEquals(TestConclusion.FAIL, testReport.getOverallTestConclusion());
    }

    @Test
    void creatingTestReportWithOneFailedResult() {
        TestReport testReport = new TestReport(UUID.randomUUID(), TEST_REPORT_DETAIL);

        TestResult testResult_1 = new TestResult("TR1", TEST_REPORT_DETAIL, TestConclusion.NO_RESULT);
        TestResult testResult_2 = new TestResult("TR2", TEST_REPORT_DETAIL, TestConclusion.PASS);
        TestResult testResult_3 = new TestResult("TR3", TEST_REPORT_DETAIL, TestConclusion.FAIL);

        testReport.add(testResult_1);
        testReport.add(testResult_2);
        testReport.add(testResult_3);

        assertNotNull(testReport);
        assertNotNull(testReport.getId());
        assertEquals(3, testReport.getTestResults().size());
        assertEquals(TestConclusion.FAIL, testReport.getOverallTestConclusion());
    }

    @Test
    void creatingTestReportWithAllPassedResults() {
        TestReport testReport = new TestReport(UUID.randomUUID(), TEST_REPORT_DETAIL);

        TestResult testResult_1 = new TestResult("TR1", TEST_REPORT_DETAIL, TestConclusion.PASS);
        TestResult testResult_2 = new TestResult("TR2", TEST_REPORT_DETAIL, TestConclusion.PASS);
        TestResult testResult_3 = new TestResult("TR3", TEST_REPORT_DETAIL, TestConclusion.PASS);

        testReport.add(testResult_1);
        testReport.add(testResult_2);
        testReport.add(testResult_3);

        assertNotNull(testReport);
        assertNotNull(testReport.getId());
        assertEquals(3, testReport.getTestResults().size());
        assertEquals(TestConclusion.PASS, testReport.getOverallTestConclusion());
    }

    @Test
    void checkIfTestResultHasTestReportReference() {
        TestReport testReport = new TestReport(UUID.randomUUID(), TEST_REPORT_DETAIL);

        TestResult testResult_1 = new TestResult("TR1", TEST_REPORT_DETAIL, TestConclusion.PASS);
        testReport.add(testResult_1);

        assertNotNull(testReport);
        assertEquals(1, testReport.getTestResults().size());

        //Test if the TestResult has the TestReport
        List<TestResult> testResultList = testReport.getTestResults();
        assertEquals(testReport.getId(), testResultList.get(0).getTestReport().getId());

    }

    @Test
    void expectedNullPointerException() {
        UUID uuid = UUID.randomUUID();
        assertThrows(NullPointerException.class, () ->
                new TestReport(uuid, null));

        assertThrows(NullPointerException.class, () ->
                new TestReport(null, TEST_REPORT_DETAIL));
    }

    @Test
    void testIfTheSameIsNotTheSame() {
        TestReport testReport_1 = new TestReport(UUID.randomUUID(), TEST_REPORT_DETAIL);
        TestReport testReport_2 = new TestReport(UUID.randomUUID(), TEST_REPORT_DETAIL);

        assertNotEquals(testReport_1, testReport_2);
    }
}
