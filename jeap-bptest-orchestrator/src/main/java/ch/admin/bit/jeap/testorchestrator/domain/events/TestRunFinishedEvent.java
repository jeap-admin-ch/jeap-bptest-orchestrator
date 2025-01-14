package ch.admin.bit.jeap.testorchestrator.domain.events;

import org.springframework.context.ApplicationEvent;

public class TestRunFinishedEvent extends ApplicationEvent {

    private final String testId;

    public TestRunFinishedEvent(Object source, String testId) {
        super(source);
        this.testId = testId;
    }
    public String getTestId() { return testId; }
}
