package ch.admin.bit.jeap.testorchestrator.services;


import ch.admin.bit.jeap.testagent.api.notification.LogDto;
import ch.admin.bit.jeap.testorchestrator.adapter.jpa.TestRunJpaRepository;
import ch.admin.bit.jeap.testorchestrator.domain.TestLog;
import ch.admin.bit.jeap.testorchestrator.domain.TestRun;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.keyValue;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogService {

    private final TestRunJpaRepository testRunJpaRepository;

    /**
     * Stores a LogDto to DB
     *
     * @param testId the Id of this TestRun
     * @param logDto the LogDto
     */
    @Transactional
    public void log(String testId, LogDto logDto) {
        log.info("Received log {}: {}", keyValue("testId", testId), logDto);
        TestRun testRun = testRunJpaRepository.getReferenceById(UUID.fromString(testId));
        TestLog testLog = new TestLog(logDto.getLogLevel(), logDto.getLogMessage(), logDto.getSource());
        testRun.add(testLog);
        testRunJpaRepository.save(testRun);
    }

}
