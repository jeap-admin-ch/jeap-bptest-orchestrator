package ch.admin.bit.jeap.testorchestrator.testsupport;

import ch.admin.bit.jeap.testagent.api.notification.NotificationDto;
import ch.admin.bit.jeap.testagent.api.prepare.PreparationDto;

import ch.admin.bit.jeap.testorchestrator.domain.events.ExecuteDoneEvent;
import ch.admin.bit.jeap.testorchestrator.domain.events.NotificationEvent;
import ch.admin.bit.jeap.testorchestrator.services.TestCaseBaseInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Support class for running test cases in unit tests. Test cases can be run without starting up a Spring context.
 */
@Slf4j
public class TestCaseRunner implements ApplicationEventPublisher {

    private static final String CALLBACK_URL = "localhost://dummy/callback";

    private final String testId = UUID.randomUUID().toString();

    private TestcaseRunState testcaseRunState = TestcaseRunState.READY;

    private TestCaseBaseInterface testCase;

    private List<CompletableFuture<?>> tasks = new ArrayList<>();

    private CompletableFuture<Void> asyncRunCompletableFuture;


    /**
     * Run the given test case. Can only be called once on a TestCaseRunner instance. Does not support asynchronous notifications.
     * @param testCase The test case to run.
     */
    public void run(TestCaseBaseInterface testCase) {
        if (testcaseRunState != TestcaseRunState.READY) {
            throw new IllegalStateException("A TestCaseRunner instance can only run one test, i.e. start() can only be called once.");
        }
        this.testcaseRunState = TestcaseRunState.STARTED;
        this.testCase = testCase;

        testCase.prepare(testId, createPreparationDto());
        testCase.execute(testId);
    }

    /**
     * Run the given test case asynchronously with support for asynchronous notifications. Can only be called once on a TestCaseRunner instance.
     * Use this run method if your tests needs asynchronous notifications.
     * @param testCase The test case to run.
     */
    public CompletableFuture<Void> runAsync(TestCaseBaseInterface testCase) {
        asyncRunCompletableFuture = new CompletableFuture<>();
        CompletableFuture<Void> runTask = CompletableFuture.runAsync(() -> run(testCase));
        addTask(runTask);
        runTask.thenRun(this::completeAsyncRunIfAllTasksDone);
        return asyncRunCompletableFuture;
    }

    /**
     * Check if the test case run has finished.
     * @return <code>true</code> if the test case run has finished, <code>false</code> otherwise.
     */
    public boolean hasFinished() {
        return testcaseRunState == TestcaseRunState.FINISHED;
    }

    /**
     * Get the the test id assigned to this test case run.
     * @return the test id assigned to this test case run
     */
    public String getTestId() {
        return testId;
    }

    /**
     * Get the callback URL provided to the test case by the test runner.
     * @return the callback URL provided to the test case by the test runner.
     */
    public String getCallbackUrl() {
        return CALLBACK_URL;
    }

    /**
     * Get the ApplicationEventPublisher instance to be used by the test case instance run by this runner.
     * Test cases communicate with the TestCaseService by publishing Spring application events. A test case run by this
     * test case runner must use this ApplicationEventPublisher for the test case runner to be able to react on the events.
     * @return The ApplicationEventPublisher instance to be used by the test case instance run by this runner.
     */
    public ApplicationEventPublisher getApplicationEventPublisher() {
        return this;
    }

    /**
     * Publish the given notification as NotificationEvent to the test case run by this test case runner.
     * Test cases listen to notification events initiated by the test agents. Use this method to simulate the
     * test agent callbacks to the test orchestrator that result in such notification events.
     *
     * @param notificationDto The notification.
     */
    public void notify(NotificationDto notificationDto) {
        log.debug("Notifying test case with '{}' notification and data '{}' from '{}'.", notificationDto.getNotification(), notificationDto.getData(), notificationDto.getProducer());
        testCase.onApplicationEvent(new NotificationEvent(this, notificationDto));
    }

    /**
     * Publish the given notification asynchronously after the given delay as NotificationEvent to the test case run by this test case runner.
     * @see #notify(NotificationDto)
     *
     * @param notificationDto The notification.
     */
    public void notifyAsync(NotificationDto notificationDto, long delay, TimeUnit unit) {
        if (asyncRunCompletableFuture == null) {
            throw new IllegalStateException("Thou shalt not call notifyAsync() outside of an async run.");
        }
        log.debug("Scheduling asynchronous notification '{}' with data '{}' from '{}' with delay of {} {}.",
                notificationDto.getNotification(), notificationDto.getData(), notificationDto.getProducer(), delay, unit);
        CompletableFuture<Void> notifyTask = CompletableFuture.runAsync(() -> notify(notificationDto),
                CompletableFuture.delayedExecutor(delay, unit));
        addTask(notifyTask);
        notifyTask.thenRun(this::completeAsyncRunIfAllTasksDone);
    }

    /**
     * Spring application event publishing implementation provided to the test case run by this runner to receive the test case events.
     */
    @Override
    public void publishEvent(Object event) {
        if (event instanceof ExecuteDoneEvent executeDoneEvent) {
            if (!testCase.getTestCaseName().equals(executeDoneEvent.getTestCaseName())) {
                throw new IllegalArgumentException("ExecuteDoneEvent's test case name does not match name of the test case under test.");
            }
            if (!testId.equals(executeDoneEvent.getTestId())) {
                throw new IllegalArgumentException("ExecuteDoneEvent's test id does not match the test id of this test run.");
            }
            log.debug("Got ExecuteDoneEvent event, ending execute phase and progressing to verify and clean-up phases.");
            finish();
        }
    }

    private void finish() {
        log.debug("Entering verify and clean-up phases.");
        testCase.verify(testId);
        testCase.cleanUp(testId);
        testcaseRunState = TestcaseRunState.FINISHED;
    }

    private void completeAsyncRunIfAllTasksDone() {
        if(allTasksDone()) {
            log.debug("All tasks done, completing async run future.");
            asyncRunCompletableFuture.complete(null);
        }
    }

    synchronized private void addTask(CompletableFuture<Void> task) {
        tasks.add(task);
    }

    synchronized private boolean allTasksDone() {
        return tasks.stream().allMatch(CompletableFuture::isDone);
    }

    private PreparationDto createPreparationDto() {
        return PreparationDto.builder()
                .callbackBaseUrl(CALLBACK_URL)
                .testCase(testCase.getTestCaseName())
                .build();
    }

    private enum TestcaseRunState {
        READY, // test case run initialized and ready to be run
        STARTED, // test case run has been started and is running
        FINISHED // test case run has been completed
    }

}
