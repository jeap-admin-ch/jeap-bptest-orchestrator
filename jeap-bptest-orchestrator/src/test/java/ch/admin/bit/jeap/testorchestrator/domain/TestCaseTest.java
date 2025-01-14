package ch.admin.bit.jeap.testorchestrator.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TestCaseTest {

    public static final String TEST_CASE_NAME = "TestCaseName";
    public static final String ZEPHYR_TEST_CASE_KEY = "ZepyhrTestCaseKey";
    public static final String JIRA_PROJECT_KEY = "jiraProjectKey";

    @Test
    void createTestCase() {

        TestCase testCase = new TestCase(TEST_CASE_NAME, JIRA_PROJECT_KEY, ZEPHYR_TEST_CASE_KEY);

        assertNotNull(testCase);
        assertEquals(TEST_CASE_NAME, testCase.getName());
        assertEquals(ZEPHYR_TEST_CASE_KEY, testCase.getZepyhrTestCaseKey());
        assertEquals(JIRA_PROJECT_KEY, testCase.getJiraProjectKey());
        //assertEquals(0, testCase.getTestRuns().size());
    }

    @Test
    void testExcpectedNullPointerException() {
        assertThrows(NullPointerException.class, () ->
                new TestCase(null, ZEPHYR_TEST_CASE_KEY, JIRA_PROJECT_KEY));

        assertThrows(NullPointerException.class, () ->
                new TestCase(TEST_CASE_NAME, null, JIRA_PROJECT_KEY));

        assertThrows(NullPointerException.class, () ->
                new TestCase(TEST_CASE_NAME, ZEPHYR_TEST_CASE_KEY, null));
    }
}
