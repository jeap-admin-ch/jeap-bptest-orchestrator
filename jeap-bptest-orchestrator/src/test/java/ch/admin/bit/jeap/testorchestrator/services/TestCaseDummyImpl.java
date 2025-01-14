package ch.admin.bit.jeap.testorchestrator.services;

import ch.admin.bit.jeap.testagent.api.prepare.PreparationDto;
import ch.admin.bit.jeap.testorchestrator.domain.events.NotificationEvent;
import lombok.Getter;

public class TestCaseDummyImpl implements TestCaseBaseInterface {
    @Getter
    private PreparationDto preparationDto;

    @Override
    public String getTestCaseName() {
        return "TestCaseDummyImpl";
    }

    @Override
    public String getJiraProjectKey() {
        return "TestJiraProjectKey";
    }

    @Override
    public String getZephyrTestCaseKey() {
        return "ZephyrTestCaseKey";
    }

    @Override
    public void prepare(String testId, PreparationDto preparationDto) {
        this.preparationDto = preparationDto;
    }

    @Override
    public void execute(String testId) {

    }

    @Override
    public void verify(String testId) {

    }

    @Override
    public void cleanUp(String testId) {

    }


    @Override
    public void onApplicationEvent(NotificationEvent notificationEvent) {

    }
}
