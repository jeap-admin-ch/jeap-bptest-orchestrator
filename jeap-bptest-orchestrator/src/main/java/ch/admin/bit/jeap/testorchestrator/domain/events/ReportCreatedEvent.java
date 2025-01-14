package ch.admin.bit.jeap.testorchestrator.domain.events;

import ch.admin.bit.jeap.testorchestrator.domain.TestConclusion;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ReportCreatedEvent extends ApplicationEvent {

    private final String testCaseName;
    private final String testId;
    private final TestConclusion conclusion;

    public ReportCreatedEvent(Object source, String testCaseName, String testId, TestConclusion conclusion) {
        super(source);
        this.testCaseName = testCaseName;
        this.testId = testId;
        this.conclusion = conclusion;
    }

}

