package ch.admin.bit.jeap.testorchestrator.adapter.testagent;

import ch.admin.bit.jeap.testorchestrator.services.TestReportService;
import ch.admin.bit.jeap.testorchestrator.services.TestRunService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
@Slf4j
public class OrchestratorAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

    private TestRunService testRunService;
    private TestReportService testReportService;

    @Autowired
    public void setTestRunService(TestRunService testRunService) {
        this.testRunService = testRunService;
    }

    @Autowired
    public void setTestReportService(TestReportService testReportService) {
        this.testReportService = testReportService;
    }

    @Override
    public void handleUncaughtException(Throwable throwable, Method method, Object... objects) {
        String testId = ((TestAgentException) throwable).getTestId();
        this.testRunService.abortTestRun(testId, throwable.getMessage());
        this.testReportService.reportToJira(testId);
    }
}
