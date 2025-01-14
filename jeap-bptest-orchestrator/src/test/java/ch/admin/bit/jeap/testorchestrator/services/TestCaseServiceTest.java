package ch.admin.bit.jeap.testorchestrator.services;

import ch.admin.bit.jeap.testorchestrator.adapter.jpa.TestCaseJpaRepository;
import ch.admin.bit.jeap.testorchestrator.adapter.jpa.TestRunJpaRepository;
import ch.admin.bit.jeap.testorchestrator.domain.TestCase;
import ch.admin.bit.jeap.testorchestrator.domain.TestRun;
import ch.admin.bit.jeap.testorchestrator.domain.TestState;
import ch.admin.bit.jeap.testorchestrator.domain.events.ExecuteDoneEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestCaseServiceTest {

    private static final String CALLBACK_URL = "CallbackUrl";
    private static final String ZEYPR_ENV = "zeyprEnv";
    private static final long TEST_RUN_TIMEOUT = 30000;

    private TestCaseService testCaseService;

    @Mock
    private TestReportService testReportService;

    @Mock
    private TestRunService testRunService;

    @Mock
    private TestCaseJpaRepository testCaseJpaRepository;

    private final List<TestCaseBaseInterface> testCaseBaseInterfaceList = new ArrayList<>();
    private TestCaseDummyImpl testCaseDummyImpl;


    @BeforeEach
    void beforeEach() {
        testCaseDummyImpl = new TestCaseDummyImpl();
        testCaseBaseInterfaceList.add(testCaseDummyImpl);
        testCaseService = new TestCaseService(testCaseBaseInterfaceList,
                testReportService,
                testRunService,
                testCaseJpaRepository,
                CALLBACK_URL,
                ZEYPR_ENV,
                TEST_RUN_TIMEOUT);
    }

    @Test
    void wrongTestCaseName() {
        assertThrows(IllegalArgumentException.class, () -> testCaseService.startTestRun("AWrongTestCaseName"));
    }

    @Test
    void startTestRun() {
        TestCase testCase = new TestCase("name", "jiraKey", "zephyrKey");
        TestRun testRun = new TestRun(ZEYPR_ENV, testCase);

        when(testCaseJpaRepository.save(any(TestCase.class))).thenReturn(testCase);
        when(testRunService.createTestRun(ZEYPR_ENV, testCase)).thenReturn(testRun);

        String testId = testCaseService.startTestRun("TestCaseDummyImpl");

        assertEquals("TestCaseDummyImpl", testCaseDummyImpl.getPreparationDto().getTestCase());
        assertDoesNotThrow(() -> UUID.fromString(testId));
        assertTrue(testCaseService.isTimerRunning(testId));
    }

    @Test
    void stopTestRun() {
        final String testCaseName = "TestCaseDummyImpl" ;
        TestCase testCase = new TestCase(testCaseName, "jiraKey", "zephyrKey");
        TestRun testRun = new TestRun(ZEYPR_ENV, testCase);

        when(testCaseJpaRepository.save(any(TestCase.class))).thenReturn(testCase);
        when(testRunService.createTestRun(ZEYPR_ENV, testCase)).thenReturn(testRun);

        String testId = testCaseService.startTestRun(testCaseName);
        testCaseService.onApplicationEvent(new ExecuteDoneEvent(testCaseService, testCaseName, testId));
        verify(testReportService).reportToJira(testId);
        assertFalse(testCaseService.isTimerRunning(testId));
    }
}
