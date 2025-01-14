package ch.admin.bit.jeap.testorchestrator.services;

import ch.admin.bit.jeap.testorchestrator.adapter.jpa.TestRunJpaRepository;
import ch.admin.bit.jeap.testorchestrator.domain.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestRunServiceTest {

    private static final String TEST_RUN_ID = UUID.randomUUID().toString();

    @Mock
    private TestRunJpaRepository testRunJpaRepository;

    @Mock
    private PlatformTransactionManager transactionManager;

    @Test
    void parameterSetAndGet() {
        when(testRunJpaRepository.getReferenceById(UUID.fromString(TEST_RUN_ID))).thenReturn(createTestRun());
        TestRunService testRunService = new TestRunService(null, testRunJpaRepository, transactionManager);

        testRunService.setParameter(TEST_RUN_ID, "key", "value");
        testRunService.setParameter(TEST_RUN_ID, "key2", "value2");

        Map<String, String> parameters = testRunService.getParameters(TEST_RUN_ID);
        String parameterValue = testRunService.getParameterValue(TEST_RUN_ID, "key");

        assertThat(parameters)
                .containsEntry("key", "value")
                .containsEntry("key2", "value2");
        assertThat(parameterValue).isEqualTo("value");
    }


    @Test
    void getOverallTestConclusion_testReportNotPresent_returnNoResult() {
        when(testRunJpaRepository.getReferenceById(UUID.fromString(TEST_RUN_ID))).thenReturn(createTestRun());
        TestRunService testRunService = new TestRunService(null, testRunJpaRepository, transactionManager);

        final TestConclusion testConclusion = testRunService.getOverallTestConclusion(TEST_RUN_ID);

        assertThat(testConclusion).isEqualTo(TestConclusion.NO_RESULT);
    }

    @Test
    void getOverallTestConclusion_testReportPass_returnPass() {
        TestRun testRun = createTestRun();
        TestReport testReport = new TestReport(UUID.randomUUID(), "test");
        testReport.add(new TestResult("test1", "test1", TestConclusion.PASS));
        testReport.add(new TestResult("test2", "test2", TestConclusion.NO_RESULT));
        testRun.setTestReport(testReport);
        when(testRunJpaRepository.getReferenceById(UUID.fromString(TEST_RUN_ID))).thenReturn(testRun);
        TestRunService testRunService = new TestRunService(null, testRunJpaRepository, transactionManager);

        final TestConclusion testConclusion = testRunService.getOverallTestConclusion(TEST_RUN_ID);

        assertThat(testConclusion).isEqualTo(TestConclusion.PASS);
    }

    @Test
    void getOverallTestConclusion_testReportFail_returnFail() {
        TestRun testRun = createTestRun();
        TestReport testReport = new TestReport(UUID.randomUUID(), "test");
        testReport.add(new TestResult("test1", "test1", TestConclusion.PASS));
        testReport.add(new TestResult("test2", "test2", TestConclusion.NO_RESULT));
        testReport.add(new TestResult("test3", "test3", TestConclusion.FAIL));
        testRun.setTestReport(testReport);
        when(testRunJpaRepository.getReferenceById(UUID.fromString(TEST_RUN_ID))).thenReturn(testRun);
        TestRunService testRunService = new TestRunService(null, testRunJpaRepository, transactionManager);

        final TestConclusion testConclusion = testRunService.getOverallTestConclusion(TEST_RUN_ID);

        assertThat(testConclusion).isEqualTo(TestConclusion.FAIL);
    }

    private TestRun createTestRun(){
        return new TestRun("ENV", new TestCase("name", "JIRA_KEY", "xs"));
    }
}
