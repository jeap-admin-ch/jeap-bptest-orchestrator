package ch.admin.bit.jeap.testorchestrator.metrics;

import ch.admin.bit.jeap.testorchestrator.adapter.jpa.TestCaseJpaRepository;
import ch.admin.bit.jeap.testorchestrator.adapter.jpa.TestRunJpaRepository;
import ch.admin.bit.jeap.testorchestrator.domain.TestConclusion;
import ch.admin.bit.jeap.testorchestrator.domain.TestRun;
import ch.admin.bit.jeap.testorchestrator.domain.TestState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TestCaseMetricsService {

    private final TestCaseJpaRepository testCaseJpaRepository;
    private final TestRunJpaRepository testRunJpaRepository;

    @Transactional(readOnly = true)
    public List<TestCaseMetricsDto> getAllTestCaseMetrics() {

        return testCaseJpaRepository.findAll().stream()
                .map(testCase -> {
                    List<TestRun> testRunList = testRunJpaRepository.findByTestCase(testCase);
                    TestRunMetricsDto testRunMetricsDto = calculateTestRuns(testRunList);
                    return new TestCaseMetricsDto(testCase.getName(),
                            testRunMetricsDto.totalTestRuns(),
                            testRunMetricsDto.successfulTestRuns(),
                            testRunMetricsDto.failedTestRuns(),
                            testRunMetricsDto.formattedAverageSuccessDuration(),
                            testRunMetricsDto.formattedAverageFailedDuration());
                })
                .collect(Collectors.toList());
    }

    private TestRunMetricsDto calculateTestRuns(List<TestRun> testRunList) {
        AtomicInteger numberOfTestRuns = new AtomicInteger();
        AtomicInteger failedTestRuns = new AtomicInteger();
        AtomicInteger successfulTestRuns = new AtomicInteger();
        List<TestRun> successfulFullTestRunList = new java.util.ArrayList<>(List.of());
        List<TestRun> failedTestRunList = new java.util.ArrayList<>(List.of());

        testRunList.forEach(testRun -> {
            TestState testState = testRun.getTestState();
            // Because TestState can have 3 States: STARTED, ABORTED and ENDED
            // We only calculate ABORTED (as Failed) and ENDED. In the State STARTED
            // we can not determine if the TestRun is FAILED or not.
            if (TestState.ABORTED.equals(testState)) {
                failedTestRuns.getAndIncrement();
                numberOfTestRuns.getAndIncrement();
                failedTestRunList.add(testRun);
            }
            else if (TestState.ENDED.equals(testState)) {
                // when the TestState is ENDED, we can not say if the all the TestStep passed
                TestConclusion testConclusion = testRun.getTestReport().getOverallTestConclusion();
                if (testConclusion.equals(TestConclusion.FAIL)) {
                    failedTestRuns.getAndIncrement();
                    numberOfTestRuns.getAndIncrement();
                    failedTestRunList.add(testRun);
                } else if (testConclusion.equals(TestConclusion.PASS)) {
                    successfulTestRuns.getAndIncrement();
                    numberOfTestRuns.getAndIncrement();
                    successfulFullTestRunList.add(testRun);
                } else if (testConclusion.equals(TestConclusion.NO_RESULT)) {
                    failedTestRuns.getAndIncrement();
                    numberOfTestRuns.getAndIncrement();
                    failedTestRunList.add(testRun);
                }
            }

        });
        return new TestRunMetricsDto(numberOfTestRuns.get(),
                successfulTestRuns.get(),
                failedTestRuns.get(),
                averageTestTime(successfulFullTestRunList),
                averageTestTime(failedTestRunList));
    }

    private Duration averageTestTime(List<TestRun> testRunList) {
        List<Duration> durationList = testRunList.stream()
                .filter(testRun -> (testRun.getEndedAt() != null))
                .map(testRun -> Duration.between(testRun.getStartedAt(), testRun.getEndedAt()))
                .toList();
        Duration totalDuration = Duration.ZERO;
        for (Duration duration : durationList) {
            totalDuration = totalDuration.plus(duration);
        }
        if (durationList.size() == 0) {
            return Duration.ZERO;
        }
        return totalDuration.dividedBy(durationList.size());
    }

}
