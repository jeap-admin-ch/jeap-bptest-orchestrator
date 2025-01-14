package ch.admin.bit.jeap.testorchestrator.metrics;

import java.time.Duration;

public record TestRunMetricsDto (int totalTestRuns,
                                 int successfulTestRuns,
                                 int failedTestRuns,
                                 Duration averageSuccessDuration,
                                 Duration averageFailedDuration) {

    private static final String TIME_FORMAT = "%02d:%02d:%02d.%02d";

    String formattedAverageSuccessDuration() {
        return TIME_FORMAT.formatted(
                averageSuccessDuration.toHours(),
                averageSuccessDuration.toMinutesPart(),
                averageSuccessDuration.toSecondsPart(),
                averageSuccessDuration.toMillisPart());
    }

    String formattedAverageFailedDuration() {
        return TIME_FORMAT.formatted(
                averageFailedDuration.toHours(),
                averageFailedDuration.toMinutesPart(),
                averageFailedDuration.toSecondsPart(),
                averageFailedDuration.toMillisPart());
    }
}
