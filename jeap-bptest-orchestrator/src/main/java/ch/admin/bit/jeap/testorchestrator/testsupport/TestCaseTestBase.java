package ch.admin.bit.jeap.testorchestrator.testsupport;

import ch.admin.bit.jeap.testorchestrator.adapter.testagent.TestAgentWebClient;
import ch.admin.bit.jeap.testorchestrator.services.TestReportService;
import ch.admin.bit.jeap.testorchestrator.services.TestRunService;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;


/**
 * Simple base class for test case unit tests. For every test method it sets up a new TestCaseRunner instance,
 * new mocks of TestAgentWebClient and TestReportService, as well as a TestCaseMockTool instance.
 * the commonly used mocks and
 */
public abstract class TestCaseTestBase {

    protected TestCaseRunner testcaseRunner;
    protected String testId;
    protected TestAgentWebClient testAgentWebClientMock;
    protected TestReportService testReportServiceMock;
    protected TestRunService testRunServiceStub;
    protected TestCaseMockTool testCaseMockTool;

    @BeforeEach
    void setUp() {
        testAgentWebClientMock = Mockito.mock(TestAgentWebClient.class);
        testReportServiceMock = Mockito.mock(TestReportService.class);
        testcaseRunner = new TestCaseRunner();
        testId = testcaseRunner.getTestId();
        testRunServiceStub = new TestRunServiceStub(testId);
        testCaseMockTool = TestCaseMockTool.builder()
                .testAgentWebClientMock(testAgentWebClientMock)
                .testReportServiceMock(testReportServiceMock)
                .testId(testcaseRunner.getTestId())
                .callBackBaseUrl(testcaseRunner.getCallbackUrl())
                .build();
    }

}
