package ch.admin.bit.jeap.testorchestrator.adapter.testagent;

/**
 * TestAgentException is a runtime Exception, which occurs when the TestAgent-RestAPI is not available,
 * does not answer in Time (&lt;5 seconds) or answers with a 4xxx or 5xx error.
 */
public class TestAgentException extends RuntimeException {

    final String testId;
    final String testAgentName;
    final String requestUrl;

    public TestAgentException(String testId,
                              String testAgentName,
                              String requestUrl,
                              String message) {
        super(message);
        this.testId = testId;
        this.testAgentName = testAgentName;
        this.requestUrl = requestUrl;
    }

    public String getTestId() {
        return testId;
    }

    public String getTestAgentName() {
        return testAgentName;
    }

    public String getRequestUrl() {
        return requestUrl;
    }
}
