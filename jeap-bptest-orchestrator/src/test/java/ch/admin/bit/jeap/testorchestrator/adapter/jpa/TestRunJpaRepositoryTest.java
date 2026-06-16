package ch.admin.bit.jeap.testorchestrator.adapter.jpa;

import ch.admin.bit.jeap.testorchestrator.domain.TestCase;
import ch.admin.bit.jeap.testorchestrator.domain.TestRun;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@ContextConfiguration(classes = JpaAdapterConfig.class)
class TestRunJpaRepositoryTest {

    private final TestRunJpaRepository repository;
    private final TestCaseJpaRepository testCaseJpaRepository;

    @Autowired
    TestRunJpaRepositoryTest(TestRunJpaRepository repository, TestCaseJpaRepository testCaseJpaRepository) {
        this.repository = repository;
        this.testCaseJpaRepository = testCaseJpaRepository;
    }

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void saveTestRunAndGetById() {
        TestCase testCase = new TestCase("TestCaseNamew", "jpk", "ztck");
        TestCase testCaseSaved = testCaseJpaRepository.saveAndFlush(testCase);
        entityManager.detach(testCaseSaved);

        TestRun testRun = new TestRun("dev", testCaseSaved);
        String testId = testRun.getTestId();
        TestRun testRunSaved = repository.saveAndFlush(testRun);

        entityManager.detach(testRunSaved);

        TestRun testRunWithId = repository.getReferenceById(UUID.fromString(testId));
        assertEquals(testId, testRunWithId.getTestId());
    }

    @Test
    void saveTestRunsAndGetTestCase() {
        TestCase testCase = new TestCase("TestCase1", "jpk", "ztck");
        TestCase testCaseSaved = testCaseJpaRepository.saveAndFlush(testCase);
        entityManager.detach(testCaseSaved);

        TestRun testRun = new TestRun("dev", testCaseSaved);
        TestRun testRunSaved = repository.saveAndFlush(testRun);

        TestRun testRun2 = new TestRun("dev", testCaseSaved);
        TestRun testRunSaved2 = repository.saveAndFlush(testRun2);

        entityManager.detach(testRunSaved);
        entityManager.detach(testRunSaved2);

        List<TestRun> testRunList = repository.findByTestCase(testCase);
        assertEquals(2, testRunList.size());

    }






}
