package ch.admin.bit.jeap.testorchestrator.domain;

import lombok.*;
import org.springframework.boot.logging.LogLevel;

import jakarta.persistence.*;
import java.time.ZonedDateTime;
import java.util.UUID;

import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PROTECTED;

/**
 * The TestLog holds logging information from the TestAgents. A TestLog is always bound to a {@link TestRun}
 */
@SuppressWarnings({"FieldMayBeFinal", "JpaDataSourceORMInspection"}) // JPA spec mandates non-final fields
@Entity
@NoArgsConstructor(access = PROTECTED) // for jpa
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TestLog {
    @Id
    @Getter
    @EqualsAndHashCode.Include
    private final UUID id = UUID.randomUUID();

    @Getter
    private ZonedDateTime createdAt = ZonedDateTime.now();

    @Enumerated(EnumType.STRING)
    @NonNull
    @Getter
    private LogLevel logLevel;

    @NonNull
    @Getter
    private String message;

    @NonNull
    @Getter
    private String source;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "test_run_id")
    @Getter(value = PACKAGE)
    @Setter(value = PACKAGE)
    private TestRun testRun;

    public TestLog(@NonNull LogLevel logLevel, @NonNull String message, @NonNull String source) {
        this.logLevel = logLevel;
        this.message = message;
        this.source = source;
    }
}
