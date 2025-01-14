package ch.admin.bit.jeap.testorchestrator.services;

import ch.admin.bit.jeap.testagent.api.verify.Conclusion;
import ch.admin.bit.jeap.testagent.api.verify.ReportDto;
import ch.admin.bit.jeap.testagent.api.verify.ResultDto;
import ch.admin.bit.jeap.testorchestrator.adapter.jpa.TestReportJpaRepository;
import ch.admin.bit.jeap.testorchestrator.adapter.jpa.TestRunJpaRepository;
import ch.admin.bit.jeap.testorchestrator.adapter.zephyr.ZephyrItemDto;
import ch.admin.bit.jeap.testorchestrator.adapter.zephyr.ZephyrStepDto;
import ch.admin.bit.jeap.testorchestrator.adapter.zephyr.ZephyrTestRunDto;
import ch.admin.bit.jeap.testorchestrator.adapter.zephyr.ZephyrWebClient;
import ch.admin.bit.jeap.testorchestrator.domain.*;
import ch.admin.bit.jeap.testorchestrator.domain.events.ReportCreatedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.ZonedDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestReportServiceTest {
    @InjectMocks
    private TestReportService testReportService;

    @Mock
    private TestRunJpaRepository testRunJpaRepository;

    @Mock
    private TestReportJpaRepository testReportJpaRepository;

    @Mock
    private ZephyrWebClient zephyrWebClient;

    @Mock
    private PlatformTransactionManager transactionManager;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;


    @Test
    void persistTestResult() {
        UUID testIdUUID = UUID.randomUUID();

        TestRun testRun = new TestRun("ENV", new TestCase("name", "JIRA_KEY", "xs"));
        when(testRunJpaRepository.getReferenceById(testIdUUID)).thenReturn(testRun);

        ArgumentCaptor<TestReport> argumentCaptor = ArgumentCaptor.forClass(TestReport.class);

        testReportService.persistTestResult(testIdUUID.toString(), createReportDto(testIdUUID));

        verify(testReportJpaRepository, times(1)).save(any(TestReport.class));
        verify(testReportJpaRepository).save(argumentCaptor.capture());

        TestReport generatedTestReport = argumentCaptor.getValue();
        assertEquals("TestId: " + testIdUUID, generatedTestReport.getDetail());
        assertEquals(1, generatedTestReport.getTestResults().size());
        TestResult testResult = generatedTestReport.getTestResults().get(0);
        assertEquals("ResultName", testResult.getName());
        assertEquals(TestConclusion.PASS, testResult.getTestConclusion());
    }

    @Test
    void reportToJira() {
        UUID testIdUUID = UUID.randomUUID();

        TestRun testRun = new TestRun("ENV", new TestCase("name", "JIRA_KEY", "xs"));
        TestReport testReport = new TestReport(testIdUUID, "TestId: "+ testRun.getTestId());
        TestResult testResult = new TestResult("TEST_RESULT_NAME", "TEST_RESULT_DETAIL", TestConclusion.FAIL);
        testReport.add(testResult);
        testRun.setTestReport(testReport);
        when(testRunJpaRepository.getReferenceById(testIdUUID)).thenReturn(testRun);

        ArgumentCaptor<ZephyrTestRunDto> argumentCaptor = ArgumentCaptor.forClass(ZephyrTestRunDto.class);

        testReportService.reportToJira(testIdUUID.toString());

        verify(zephyrWebClient, times(1)).testrun(any(ZephyrTestRunDto.class));
        verify(zephyrWebClient).testrun(argumentCaptor.capture());

        ZephyrTestRunDto generatedZephyrTestRunDto = argumentCaptor.getValue();

        assertEquals("BusinessProcess Test Cycle", generatedZephyrTestRunDto.getName());
        assertEquals("JIRA_KEY", generatedZephyrTestRunDto.getProjectKey());
        assertEquals(1, generatedZephyrTestRunDto.getItems().size());

        ZephyrItemDto zephyrItemDto = generatedZephyrTestRunDto.getItems().get(0);
        assertEquals("ENV", zephyrItemDto.getEnvironment());
        assertEquals("Fail", zephyrItemDto.getStatus());
        assertEquals(1, zephyrItemDto.getScriptResults().size());

        ZephyrStepDto zephyrStepDto = zephyrItemDto.getScriptResults().get(0);
        assertEquals("Fail", zephyrStepDto.getStatus());
        assertEquals("TEST_RESULT_DETAIL", zephyrStepDto.getComment());
        assertEquals(0, zephyrStepDto.getIndex());

        verify(applicationEventPublisher).publishEvent(any(ReportCreatedEvent.class));
    }

    private ReportDto createReportDto(UUID testId) {

        ResultDto resultDto = ResultDto.builder()
                .conclusion(Conclusion.PASS)
                .name("ResultName")
                .detail("Detail")
                .build();

        return ReportDto.builder()
                .testId(testId.toString())
                .testcase("TestCase")
                .dateTime(ZonedDateTime.now())
                .result(resultDto)
                .build();

    }
}
