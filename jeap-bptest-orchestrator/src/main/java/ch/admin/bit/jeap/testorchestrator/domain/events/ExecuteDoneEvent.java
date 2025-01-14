package ch.admin.bit.jeap.testorchestrator.domain.events;

import org.springframework.context.ApplicationEvent;

public class ExecuteDoneEvent extends ApplicationEvent {

    private final String testCaseName;
    private final String testId;

    public ExecuteDoneEvent(Object source, String testCaseName, String testId) {
        super(source);
        this.testCaseName = testCaseName;
        this.testId = testId;
    }
    public String getTestCaseName() {
        return testCaseName;
    }

    public String getTestId() { return testId; }
}
