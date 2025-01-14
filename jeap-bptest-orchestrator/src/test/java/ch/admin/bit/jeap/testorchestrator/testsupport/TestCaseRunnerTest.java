package ch.admin.bit.jeap.testorchestrator.testsupport;

import ch.admin.bit.jeap.testagent.api.notification.NotificationDto;
import ch.admin.bit.jeap.testagent.api.prepare.PreparationDto;
import ch.admin.bit.jeap.testorchestrator.domain.events.ExecuteDoneEvent;
import ch.admin.bit.jeap.testorchestrator.domain.events.NotificationEvent;
import ch.admin.bit.jeap.testorchestrator.services.TestCaseBaseInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class TestCaseRunnerTest {

    private static final String TEST_CASE_NAME = "test";
    private static final String NOTIFICATION_NAME = "some-notification";
    private static final String NOTIFICATION_PRODUCER = "some-poducer";
    private static final Map<String, String> NOTIFICATION_DATA = Map.of("some-data-name", "some-data-value");

    private TestCaseBaseInterface testCaseMock;
    private TestCaseRunner testCaseRunner;
    private String testId;

    @BeforeEach
    void setUp() {
        testCaseRunner = new TestCaseRunner();
        testId = testCaseRunner.getTestId();
        testCaseMock = Mockito.mock(TestCaseBaseInterface.class);
        Mockito.when(testCaseMock.getTestCaseName()).thenReturn(TEST_CASE_NAME);
    }

    @Test
    void testRun_WhenRan_InitiatesPrepareAndExecutePhase()  {
        ArgumentCaptor<PreparationDto> preparationDtoCaptor = ArgumentCaptor.forClass(PreparationDto.class);

        testCaseRunner.run(testCaseMock);

        Mockito.verify(testCaseMock).prepare(eq(testId), preparationDtoCaptor.capture());
        PreparationDto preparationDto = preparationDtoCaptor.getValue();
        assertThat(preparationDto.getTestCase()).isEqualTo(TEST_CASE_NAME);
        assertThat(preparationDto.getCallbackBaseUrl()).isEqualTo(testCaseRunner.getCallbackUrl());
        assertThat(preparationDto.getData()).isNullOrEmpty();
        Mockito.verify(testCaseMock).execute(eq(testId));
        Mockito.verify(testCaseMock).getTestCaseName();
        Mockito.verifyNoMoreInteractions(testCaseMock);
        assertThat(testCaseRunner.hasFinished()).isFalse();
    }

    @Test
    void testRun_WhenCalledMoreThanOnce_ThenThrowsException()  {
        testCaseRunner.run(testCaseMock);
        assertThatThrownBy(
                () -> testCaseRunner.run(testCaseMock)
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void testNotify() {
        NotificationDto notificationDto = createNotificationDto(NOTIFICATION_NAME, testId);
        testCaseRunner.run(testCaseMock);

        testCaseRunner.notify(notificationDto);

        ArgumentCaptor<NotificationEvent> notificationEventCaptor = ArgumentCaptor.forClass(NotificationEvent.class);
        Mockito.verify(testCaseMock).onApplicationEvent(notificationEventCaptor.capture());
        NotificationEvent notificationEvent = notificationEventCaptor.getValue();
        assertThat(notificationEvent.getTestId()).isEqualTo(testId);
        assertThat(notificationEvent.getNotification()).isEqualTo(NOTIFICATION_NAME);
        assertThat(notificationEvent.getProducer()).isEqualTo(NOTIFICATION_PRODUCER);
        assertThat(notificationEvent.getData()).isEqualTo(NOTIFICATION_DATA);
    }

    @Test
    void testPublishEvent_WhenExecuteDoneEventReceived_ThenInitiateVerifyAndCleanupPhases() {
        ExecuteDoneEvent executeDoneEvent = new ExecuteDoneEvent(this, TEST_CASE_NAME,testId);
        testCaseRunner.run(testCaseMock);

        testCaseRunner.publishEvent(executeDoneEvent);

        Mockito.verify(testCaseMock).verify(eq(testId));
        Mockito.verify(testCaseMock).cleanUp(eq(testId));
        assertThat(testCaseRunner.hasFinished()).isTrue();
    }

    @Test
    void testRunAsync() throws InterruptedException, ExecutionException, TimeoutException {
        final String firsNotification = "first";
        final String secondNotification = "second";

        // When the test case runner starts the execute phase on the test case mock then schedule two asynchronous notifications,
        // one (secondNotification) being notified later than the other (firstNotification).
        Mockito.doAnswer(invocation -> {
            testCaseRunner.notifyAsync(createNotificationDto(secondNotification, testId), 500, TimeUnit.MILLISECONDS);
            testCaseRunner.notifyAsync(createNotificationDto(firsNotification, testId), 250, TimeUnit.MILLISECONDS);
            return null;
        })
        .when(testCaseMock).execute(testId);

        // When the second notification gets notified to the test case mock then publish an ExecuteDoneEvent in order to
        // proceed to the verify and cleanup phase.
        Mockito.doAnswer(invocation -> {
            NotificationEvent event = invocation.getArgument(0);
            if (event.getNotification().equals(secondNotification)) {
                testCaseRunner.getApplicationEventPublisher().publishEvent(new ExecuteDoneEvent(this, TEST_CASE_NAME, testId));
            }
            return null;
        })
        .when(testCaseMock).onApplicationEvent(any());

        testCaseRunner.runAsync(testCaseMock).get(1, TimeUnit.SECONDS);

        ArgumentCaptor<NotificationEvent> notificationEventCaptor = ArgumentCaptor.forClass(NotificationEvent.class);
        Mockito.verify(testCaseMock, Mockito.times(2)).onApplicationEvent(notificationEventCaptor.capture());
        assertThat(notificationEventCaptor.getAllValues().get(0).getNotification()).isEqualTo(firsNotification);
        assertThat(notificationEventCaptor.getAllValues().get(1).getNotification()).isEqualTo(secondNotification);
        assertThat(testCaseRunner.hasFinished()).isTrue();
    }

    @Test
    void testRunAsync_WhenAsyncNotificationPending_ThenDontComplete() {
        // When the test case runner starts the execute phase on the test case mock then schedule an asynchronous notification
        // delayed by 500 millis.
        Mockito.doAnswer(invocation -> {
            testCaseRunner.notifyAsync(createNotificationDto(NOTIFICATION_NAME, testId), 500, TimeUnit.MILLISECONDS);
            return null;
        })
        .when(testCaseMock).execute(testId);

        // When the scheduled notification gets notified to the test case mock then publish an ExecuteDoneEvent in order to
        // proceed to the verify and cleanup phase.
        Mockito.doAnswer(invocation -> {
            NotificationEvent event = invocation.getArgument(0);
            if (event.getNotification().equals(NOTIFICATION_NAME)) {
                testCaseRunner.getApplicationEventPublisher().publishEvent(new ExecuteDoneEvent(this, TEST_CASE_NAME, testId));
            }
            return null;
        })
        .when(testCaseMock).onApplicationEvent(any());

        // Assert a TimeoutException is thrown when the test case run is expected to complete before all scheduled notifications
        // have been processed (250 millis < 500 millis).
        assertThatThrownBy(
                () -> testCaseRunner.runAsync(testCaseMock).get(250, TimeUnit.MILLISECONDS)
        ).isInstanceOf(TimeoutException.class);

    }

    private NotificationDto createNotificationDto(String notification, String testId) {
        return NotificationDto.builder()
                .testId(testId)
                .notification(notification)
                .producer(NOTIFICATION_PRODUCER)
                .data(NOTIFICATION_DATA)
                .build();
    }

}
