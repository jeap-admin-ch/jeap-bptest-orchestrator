package ch.admin.bit.jeap.testorchestrator.metrics;

public record TestCaseMetricsDto(String testCaseName,
                                 int totalTestRuns,
                                 int successfulTestRuns,
                                 int failedTestRuns,
                                 String averageSuccessDuration,
                                 String averageFailedDuration) {}
