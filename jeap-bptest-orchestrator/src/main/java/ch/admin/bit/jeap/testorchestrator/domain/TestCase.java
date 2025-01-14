package ch.admin.bit.jeap.testorchestrator.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.util.UUID;

import static lombok.AccessLevel.PROTECTED;

/**
 * A TestCase holds the general Information for several TestRun's:
 *
 * - name: of the Testcase
 * - jiraProjectKey: A Jira Projects has to exist for reporting the TestResults.
 * - zepyhrTestCaseKey: A TestCase in Jira-Zeyphr has to exist for reporting the Test Results
 */
@Entity
@NoArgsConstructor(access = PROTECTED) // for jpa
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TestCase {
    @Id
    @Getter
    @EqualsAndHashCode.Include
    private final UUID id = UUID.randomUUID();

    @Column(unique = true)
    @NonNull
    @Getter
    private String name;

    @NonNull
    @Getter
    private String jiraProjectKey;

    @NonNull
    @Getter
    private String zepyhrTestCaseKey;

    /**
     * Constructor with mandatory params
     * @param name: Name of the TestCase
     * @param jiraProjectKey: JiraProjectKey for for Reporting
     * @param zepyhrTestCaseKey TestCase Issue Key for Reporting
     */
    public TestCase(@NonNull String name, @NonNull String jiraProjectKey, @NonNull String zepyhrTestCaseKey) {
        this.name = name;
        this.jiraProjectKey = jiraProjectKey;
        this.zepyhrTestCaseKey = zepyhrTestCaseKey;
    }

}
