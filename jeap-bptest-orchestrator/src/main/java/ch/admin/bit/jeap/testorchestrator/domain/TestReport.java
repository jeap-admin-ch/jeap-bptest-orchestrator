package ch.admin.bit.jeap.testorchestrator.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static lombok.AccessLevel.PROTECTED;

/**
 * A Test Report is the summary of all {@link TestResult}s. Each Result has a {@link TestConclusion}.
 */
@Entity
@NoArgsConstructor(access = PROTECTED) // for jpa
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TestReport {
    @Id
    @Getter
    @NonNull
    @EqualsAndHashCode.Include
    private UUID id;

    @NonNull
    @Getter
    @Setter
    private String detail;

    @OneToMany(
            cascade = CascadeType.ALL,
            mappedBy = "testReport",
            orphanRemoval = true)
    @Getter
    private final List<TestResult> testResults = new ArrayList<>();

    /**
     * Gets the summary-Conclusion of all TestResults
     *
     * @return
     * - TestConclusion.NO_RESULT: When there are no Results are attached
     * - TestConclusion.PASS: When every Result is PASS
     * - TestConclusion.FAIL: When one of the Result is FAIL
     */
    public TestConclusion getOverallTestConclusion() {
        long failed;
        if (testResults.isEmpty()) return TestConclusion.NO_RESULT;
        failed = testResults.stream()
                .filter(testResult -> testResult.getTestConclusion().equals(TestConclusion.FAIL))
                .count();

        if (failed > 0) {
            return TestConclusion.FAIL;
        }
        return TestConclusion.PASS;
    }

    /**
     * Adds a {@link TestResult}
     *
     * @param testResult - Entity
     */
    public void add(TestResult testResult) {
        testResult.setTestReport(this);
        testResults.add(testResult);
    }

    /**
     * Remove a {@link TestResult}
     *
     * @param index - integer
     */
    public void remove(int index) {
        testResults.remove(0);
    }

    /**
     * Constructor
     *
     * @param id     as UUID
     * @param detail DetailText
     */
    public TestReport(@NonNull UUID id, @NonNull String detail) {
        this.id = id;
        this.detail = detail;
    }
}
