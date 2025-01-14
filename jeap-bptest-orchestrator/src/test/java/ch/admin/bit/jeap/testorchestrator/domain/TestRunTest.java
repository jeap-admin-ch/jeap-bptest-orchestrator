package ch.admin.bit.jeap.testorchestrator.domain;

import org.junit.jupiter.api.Test;
import org.springframework.boot.logging.LogLevel;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TestRunTest {

    public static final String TEST_CASE_NAME = "TestCaseName";

    public static final String TEST_RUN_ENV = "DEV";

    @Test
    void creatingTestRun() {
        TestRun testRun = createTestRun();
        assertNotNull(testRun);
        assertNotNull(testRun.getTestId());
        assertNotNull(testRun.getStartedAt());
        assertNull(testRun.getEndedAt());
        assertEquals(TEST_CASE_NAME, testRun.getTestCase().getName());
        assertEquals(TEST_RUN_ENV, testRun.getEnvironment());
        assertEquals(TestState.STARTED, testRun.getTestState());
    }

    @Test
    void addTestLogToTestRun() {
        TestRun testRun = createTestRun();

        TestLog testLog_1 = new TestLog(LogLevel.INFO, "Msg", "Source");

        testRun.add(testLog_1);

        assertNotNull(testRun);
        assertEquals(1, testRun.getTestLogs().size());
    }

    @Test
    void endTestrun() {
        TestRun testRun = createTestRun();
        testRun.endTestRun(TestState.ABORTED);

        assertNotNull(testRun.getEndedAt());

    }

    @Test
    void testIfTheSameIsNotTheSame() {
        TestRun testRun_1 = createTestRun();
        TestRun testRun_2 = createTestRun();

        assertNotEquals(testRun_1, testRun_2);
    }

    @Test
    void addAndReadParameters() {
        TestRun testRun = createTestRun();
        Map<String, String> parameters = Map.of("k1", "v1", "k2", "v2");
        testRun.setParameters(parameters);

        assertEquals(parameters, testRun.getParameters());

    }

    private static TestRun createTestRun() {
        return new TestRun(
                TEST_RUN_ENV,
                new TestCase(TEST_CASE_NAME, "jpk", "ztck")
        );
    }
}
