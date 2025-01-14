package ch.admin.bit.jeap.testorchestrator.domain;

/**
 * States of a Testrun
 * {@link #STARTED}: The TestRun has started.
 * {@link #ENDED}: The TestRun has ended. This says nothing about the TestResult.
 * {@link #ABORTED}: The TestRun has aborted. Mostly due timeouts
 */
public enum TestState {
    STARTED, ENDED, ABORTED
}
