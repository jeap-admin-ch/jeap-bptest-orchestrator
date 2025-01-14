package ch.admin.bit.jeap.testorchestrator.domain;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import jakarta.persistence.*;
import java.time.ZonedDateTime;
import java.util.*;

import static lombok.AccessLevel.PROTECTED;
import static net.logstash.logback.argument.StructuredArguments.keyValue;

/**
 * A TestRun is a single Test Execution and has a report
 */
@SuppressWarnings({"FieldMayBeFinal", "JpaDataSourceORMInspection"}) // JPA spec mandates non-final fields
@Entity
@NoArgsConstructor(access = PROTECTED) // for jpa
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Slf4j
@Getter
public class TestRun {
    @Id
    @EqualsAndHashCode.Include
    private final UUID id = UUID.randomUUID();

    @Enumerated(EnumType.STRING)
    @NonNull
    @Setter
    private TestState testState;

    @NonNull
    private ZonedDateTime startedAt;

    @Setter
    private ZonedDateTime endedAt;

    private String environment;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "test_case_id")
    private TestCase testCase;

    @ElementCollection
    @CollectionTable(name = "test_run_parameters",
            joinColumns = {@JoinColumn(name = "id")})
    @MapKeyColumn(name = "name")
    @Column(name = "value_")
    @Setter
    private Map<String, String> parameters = new HashMap<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "testRun")
    @Setter
    private List<TestLog> testLogs = new ArrayList<>();


    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "test_report_id", referencedColumnName = "id")
    @Setter
    private TestReport testReport;

    /**
     * Constructor for a TestRun.
     *
     * @param environment can be null. Otherwise has to exists in as Zepyhr-Environment in the Jira-Project
     */
    public TestRun(String environment, TestCase testCase) {
        this.testState = TestState.STARTED;
        this.startedAt = ZonedDateTime.now();

        this.environment = Objects.requireNonNullElse(environment, "");
        this.testCase = Objects.requireNonNull(testCase);
    }

    /**
     * Return id as String. In the context of the Orchestrator the Id of a TestRun is called as testId
     *
     * @return testId as String.
     */
    public String getTestId() {
        return id.toString();
    }

    /**
     * Add's a TestLog to a TestRun
     *
     * @param testLog as TestLog-Entity
     */
    public void add(TestLog testLog) {
        testLog.setTestRun(this);
        this.testLogs.add(testLog);
    }

    /**
     * Ends a TestRun . Sets endDate and endState
     *
     * @param testState ABORTED or ENDED. With STARTED nothing happens
     */
    public void endTestRun(TestState testState) {
        if (testState != TestState.STARTED) {
            this.setTestState(testState);
            this.setEndedAt(ZonedDateTime.now());
        } else {
            log.warn("Trying to endTestRun {} with State STARTED. That makes no sense.", keyValue("testId", id));
        }
    }
}
