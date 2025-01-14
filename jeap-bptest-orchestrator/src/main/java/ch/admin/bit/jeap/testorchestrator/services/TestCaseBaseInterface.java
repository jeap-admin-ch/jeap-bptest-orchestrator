package ch.admin.bit.jeap.testorchestrator.services;

import ch.admin.bit.jeap.testagent.api.prepare.PreparationDto;
import ch.admin.bit.jeap.testorchestrator.domain.events.NotificationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;

/**
 * BaseInterface for a specific TestCase
 */
public interface TestCaseBaseInterface extends ApplicationListener<NotificationEvent> {

    /**
     * Name of the TestCase. Has to be exactly the same as the Classname of the specific TestCaseImplementation
     * @return testCaseName
     */
    String getTestCaseName();

    /**
     * The of the Jira-Project, where the where the Test Reports are kept
     * @return jiraProjectKey
     */
    String getJiraProjectKey();

    /**
     * Issue Key of the Zeypr TestCase
     * @return TestCase JiraIssue-key
     */
    String getZephyrTestCaseKey();


    /**
     * Prepare
     * To do inside implementation:
     * - Optional: Add additional data to the preparationDto
     * - Call 'prepare' on each TestAgent --&gt; testAgentWebClient.prepare(..)
     *
     * @param testId         the Id of this TestRun
     * @param preparationDto which you can add data (key, value)
     */
    void prepare(String testId, PreparationDto preparationDto);

    /**
     * Execute:
     * This call is async.
     *
     * To do inside implementation:
     * - Build your own ActionDto
     * - Call 'act' on each TestAgent --&gt; testAgentWebClient.act(..)
     *
     * @param testId the Id of this TestRun
     */
    @Async
    void execute(String testId);

    /**
     * Verify
     * To do inside implementation:
     * - Call 'verify' on each TestAgent --&gt; testAgentWebClient.verify(..)
     * - Store Report to Db --&gt; testReportService.persistTestResult(..)
     *
     * @param testId the Id of this TestRun
     */
    void verify(String testId);

    /**
     * CleanUp
     * To do inside implementation:
     * - Call 'delete' on each Testagent --&gt; testAgentWebClient.delete(..)
     * @param testId the Id of this TestRun
     */
    void cleanUp(String testId);
}
