package ch.admin.bit.jeap.testorchestrator.services;

import ch.admin.bit.jeap.testagent.api.notification.NotificationDto;
import ch.admin.bit.jeap.testorchestrator.domain.events.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void notify(String testId, NotificationDto notificationDto) {
        log.info("Received notification {}: {}", kv("testId", testId), notificationDto);
        NotificationEvent notificationEvent = new NotificationEvent(this, notificationDto);
        applicationEventPublisher.publishEvent(notificationEvent);
    }
}
