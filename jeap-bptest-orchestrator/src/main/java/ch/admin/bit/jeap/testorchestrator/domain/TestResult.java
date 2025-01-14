package ch.admin.bit.jeap.testorchestrator.domain;

import lombok.*;

import jakarta.persistence.*;
import java.util.UUID;

import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PROTECTED;

/**
 * A TestResult
 */
@SuppressWarnings({"FieldMayBeFinal", "JpaDataSourceORMInspection"}) // JPA spec mandates non-final fields
@Entity
@NoArgsConstructor(access = PROTECTED) // for jpa
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TestResult {
    @Id
    @Getter
    @EqualsAndHashCode.Include
    private final UUID id = UUID.randomUUID();

    @NonNull
    @Getter
    private String name;

    @Getter
    private String detail;

    @Enumerated(EnumType.STRING)
    @NonNull
    @Getter
    private TestConclusion testConclusion;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_report_id")
    @Getter(value = PACKAGE)
    @Setter(value = PACKAGE)
    private TestReport testReport;

    public TestResult(@NonNull String name, String detail, @NonNull TestConclusion testConclusion) {
        this.name = name;
        this.detail = detail;
        this.testConclusion = testConclusion;
    }
}
