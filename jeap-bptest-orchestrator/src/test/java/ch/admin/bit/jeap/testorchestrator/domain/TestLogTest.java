package ch.admin.bit.jeap.testorchestrator.domain;

import org.junit.jupiter.api.Test;
import org.springframework.boot.logging.LogLevel;

import static org.junit.jupiter.api.Assertions.*;

class TestLogTest {

    public static final String LOG_SOURCE = "The Source (TestAgent) where the LogMessage comes from";
    public static final String LOG_MESSAGE = "The LogMessage";

    @Test
    void creatingTestLog() {
        TestLog testLog = new TestLog(LogLevel.INFO, LOG_MESSAGE, LOG_SOURCE);

        assertNotNull(testLog);
        assertEquals(LOG_SOURCE, testLog.getSource());
        assertEquals(LOG_MESSAGE, testLog.getMessage());
        assertEquals(LogLevel.INFO, testLog.getLogLevel());
        assertNull(testLog.getTestRun());
        assertNotNull(testLog.getId());
        assertNotNull(testLog.getCreatedAt());
    }

    @Test
    void excpectedNullPointerException() {
        assertThrows(NullPointerException.class, () ->
               new TestLog(null, LOG_MESSAGE, LOG_SOURCE));

        assertThrows(NullPointerException.class, () ->
                new TestLog(LogLevel.INFO, null, LOG_SOURCE));

        assertThrows(NullPointerException.class, () ->
                new TestLog(LogLevel.INFO, LOG_MESSAGE, null));
    }

    @Test
    void testIfTheSameIsNotTheSame() {
        TestLog testLog_1 = new TestLog(LogLevel.INFO, LOG_MESSAGE, LOG_SOURCE);
        TestLog testLog_2 = new TestLog(LogLevel.INFO, LOG_MESSAGE, LOG_SOURCE);

        assertNotEquals(testLog_1, testLog_2);
    }

}
