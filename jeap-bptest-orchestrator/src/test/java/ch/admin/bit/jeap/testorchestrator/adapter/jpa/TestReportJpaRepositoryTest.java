package ch.admin.bit.jeap.testorchestrator.adapter.jpa;

import ch.admin.bit.jeap.testorchestrator.domain.TestConclusion;
import ch.admin.bit.jeap.testorchestrator.domain.TestReport;
import ch.admin.bit.jeap.testorchestrator.domain.TestResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@ContextConfiguration(classes = JpaAdapterConfig.class)
class TestReportJpaRepositoryTest {

    @Autowired
    private TestReportJpaRepository repository;

    @PersistenceContext
    EntityManager entityManager;

    @Test
    void saveWithNoTestResult() {
        TestReport testReport = new TestReport(UUID.randomUUID(), "Detail");
        TestReport testReportSaved = repository.saveAndFlush(testReport);

        entityManager.detach(testReportSaved);

        assertEquals(TestConclusion.NO_RESULT, testReportSaved.getOverallTestConclusion());
    }

    @Test
    void save_ThenGetOverallConculsion() {
        TestReport testReport = new TestReport(UUID.randomUUID(), "Detail");
        TestResult testResult_1 = new TestResult("TestResult1", "TestResultDetail", TestConclusion.FAIL);
        TestResult testResult_2 = new TestResult("TestResult2", "TestResultDetail", TestConclusion.PASS);
        testReport.add(testResult_1);
        testReport.add(testResult_2);
        TestReport testReportSaved = repository.saveAndFlush(testReport);

        entityManager.detach(testReportSaved);

        assertEquals(TestConclusion.FAIL, testReportSaved.getOverallTestConclusion());
    }


}
