package ch.admin.bit.jeap.testorchestrator.domain.events;

import ch.admin.bit.jeap.testagent.api.notification.NotificationDto;
import org.springframework.context.ApplicationEvent;

import java.util.Map;

public class NotificationEvent extends ApplicationEvent {
    private final NotificationDto notificationDto;

    public NotificationEvent(Object source, NotificationDto notificationDto) {
        super(source);
        this.notificationDto = notificationDto;
    }

    public String getNotification() {
        return notificationDto.getNotification();
    }

    public String getProducer() {
        return notificationDto.getProducer();
    }

    public String getTestId() {
        return notificationDto.getTestId();
    }

    public Map<String, String> getData() {
        return notificationDto.getData();
    }
}
