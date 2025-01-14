package ch.admin.bit.jeap.testorchestrator.services;

import ch.admin.bit.jeap.testagent.api.notification.LogDto;
import ch.admin.bit.jeap.testorchestrator.adapter.jpa.TestRunJpaRepository;
import ch.admin.bit.jeap.testorchestrator.domain.TestCase;
import ch.admin.bit.jeap.testorchestrator.domain.TestRun;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ExtendWith(OutputCaptureExtension.class)
class LogServiceTest {

    private final static String LOG_MESSAGE = "This is the Message";

    @InjectMocks
    private LogService logService;

    @Mock
    private TestRunJpaRepository testRunJpaRepository;

    @Test
    void logServiceTest(CapturedOutput output) {
        UUID testRunUUID = UUID.randomUUID();

        LogDto logDto = LogDto.builder()
                .logLevel(LogLevel.INFO)
                .logMessage(LOG_MESSAGE)
                .source("The Source from where to Log is sent")
                .build();

        TestRun testRun = new TestRun("ENC", new TestCase("name", "xx", "xs"));
        when(testRunJpaRepository.getReferenceById(testRunUUID)).thenReturn(testRun);

        this.logService.log(testRunUUID.toString(), logDto);
        assertThat(output).contains(LOG_MESSAGE);
    }
}






