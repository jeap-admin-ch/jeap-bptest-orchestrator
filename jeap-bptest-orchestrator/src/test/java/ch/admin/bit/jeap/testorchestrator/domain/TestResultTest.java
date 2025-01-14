package ch.admin.bit.jeap.testorchestrator.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TestResultTest {

    private static final String TEST_RESULT_DETAIL = "TestResultDetail";
    private static final String TEST_RESULT_NAME = "TestResultName";

    @Test
    void creatingTestResult() {
        TestResult testResult = new TestResult(TEST_RESULT_NAME, TEST_RESULT_DETAIL, TestConclusion.FAIL);

        assertNotNull(testResult);
        assertNotNull(testResult.getId());
        assertEquals(TEST_RESULT_DETAIL, testResult.getDetail());
        assertEquals(TEST_RESULT_NAME, testResult.getName());
    }

    @Test
    void expectedNullPointerException() {
        assertThrows(NullPointerException.class, () ->
                new TestResult(null, TEST_RESULT_DETAIL, TestConclusion.FAIL));

        assertThrows(NullPointerException.class, () ->
                new TestResult(TEST_RESULT_NAME, TEST_RESULT_DETAIL, null));
    }

    @Test
    void creationOk() {
        TestResult testResult = new TestResult(TEST_RESULT_NAME, null, TestConclusion.FAIL);
        assertNotNull(testResult);
    }

    @Test
    void testIfTheSameIsNotTheSame() {
        TestResult testResult_1 = new TestResult(TEST_RESULT_NAME, TEST_RESULT_DETAIL, TestConclusion.FAIL);
        TestResult testResult_2 = new TestResult(TEST_RESULT_NAME, TEST_RESULT_DETAIL, TestConclusion.FAIL);

        assertNotEquals(testResult_2, testResult_1);
    }

}
