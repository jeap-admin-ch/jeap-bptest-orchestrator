package ch.admin.bit.jeap.testorchestrator.services;


import ch.admin.bit.jeap.testagent.api.verify.Conclusion;
import ch.admin.bit.jeap.testagent.api.verify.ReportDto;
import ch.admin.bit.jeap.testorchestrator.adapter.jpa.TestReportJpaRepository;
import ch.admin.bit.jeap.testorchestrator.adapter.jpa.TestRunJpaRepository;
import ch.admin.bit.jeap.testorchestrator.adapter.zephyr.ZephyrItemDto;
import ch.admin.bit.jeap.testorchestrator.adapter.zephyr.ZephyrStepDto;
import ch.admin.bit.jeap.testorchestrator.adapter.zephyr.ZephyrTestRunDto;
import ch.admin.bit.jeap.testorchestrator.adapter.zephyr.ZephyrWebClient;
import ch.admin.bit.jeap.testorchestrator.domain.*;
import ch.admin.bit.jeap.testorchestrator.domain.events.ReportCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestReportService {

    private final TestRunJpaRepository testRunJpaRepository;
    private final TestReportJpaRepository testReportJpaRepository;
    private final ZephyrWebClient zephyrWebClient;
    private final PlatformTransactionManager transactionManager;
    private final ApplicationEventPublisher applicationEventPublisher;
    /**
     * Persists to ReportDto and attached ResultDto's the DB
     *
     * @param testId    the particular TestRun identification
     * @param reportDto as Result from the TestAgent verify call
     */
    public void persistTestResult(String testId, ReportDto reportDto) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.executeWithoutResult(status -> {
            //1. Look if the TestRun has a Report, otherwise create one
            Optional<TestReport> testReportOptional = testReportJpaRepository.findById(UUID.fromString(testId));
            TestReport actualTestReport = testReportOptional.orElseGet(() -> createTestReport(testId, reportDto));

            //2. Iterate over the Results and Persist them
            reportDto.getResults().forEach(resultDto -> {
                TestResult testResult = new TestResult(
                        resultDto.getName(),
                        resultDto.getDetail(),
                        convertConclusionEnum(resultDto.getConclusion()));
                actualTestReport.add(testResult);
            });
            testReportJpaRepository.save(actualTestReport);
        });
    }

    /**
     * Convert the ReportEnities the Jira/Zeyphr-Format and send it to Jira
     *
     * @param testId the particular TestRun identification
     */
    public void reportToJira(String testId) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.executeWithoutResult(status -> {

            TestRun testRun = testRunJpaRepository.getReferenceById(UUID.fromString(testId));
            TestCase testCase = testRun.getTestCase();
            TestReport testReport = testRun.getTestReport();

            List<TestResult> testResultList = testReport.getTestResults();
            List<ZephyrStepDto> zephyrStepDtoList = new ArrayList<>();
            int index = 0;
            for (TestResult testResult : testResultList) {
                ZephyrStepDto build = ZephyrStepDto.builder()
                        .index(index++)
                        .status(convertForZepyhr(testResult.getTestConclusion()))
                        .comment(testResult.getDetail())
                        .build();
                zephyrStepDtoList.add(build);
            }

            ZephyrItemDto zephyrItemDto = ZephyrItemDto.builder()
                    .testCaseKey(testCase.getZepyhrTestCaseKey())
                    .status(convertForZepyhr(testReport.getOverallTestConclusion()))
                    .environment(testRun.getEnvironment())
                    .comment(testReport.getDetail())
                    .scriptResults(zephyrStepDtoList)
                    .build();

            ZephyrTestRunDto zephyrTestRunDto = ZephyrTestRunDto.builder()
                    .name("BusinessProcess Test Cycle")
                    .projectKey(testCase.getJiraProjectKey())
                    .items(List.of(zephyrItemDto))
                    .build();

            zephyrWebClient.testrun(zephyrTestRunDto);

            applicationEventPublisher.publishEvent(new ReportCreatedEvent(this, testCase.getName(), testId, testReport.getOverallTestConclusion()));
        });


    }

    /**
     * Creates and persists a TestReport
     *
     * @param testId    TestRun identification
     * @param reportDto from the TestAgent
     * @return TestReport Domain Entity
     */
    private TestReport createTestReport(String testId, ReportDto reportDto) {
        TestRun testRun = testRunJpaRepository.getReferenceById(UUID.fromString(testId));

        TestReport newTestReport = new TestReport(UUID.fromString(testId),
                "TestId: " + reportDto.getTestId());

        testRun.setTestReport(newTestReport);
        this.testRunJpaRepository.save(testRun);
        return testRun.getTestReport();
    }

    private static String convertForZepyhr(TestConclusion testConclusion) {
        if (TestConclusion.PASS.equals(testConclusion)) return "Pass";
        return "Fail";
    }

    private static TestConclusion convertConclusionEnum(Conclusion apiConclusion) {
        return switch (apiConclusion) {
            case PASS -> TestConclusion.PASS;
            case FAIL -> TestConclusion.FAIL;
        };
    }

}
