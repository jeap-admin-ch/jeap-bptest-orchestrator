package ch.admin.bit.jeap.testorchestrator.adapter.jpa;


import ch.admin.bit.jeap.testorchestrator.domain.TestCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@DataJpaTest
@ContextConfiguration(classes = JpaAdapterConfig.class)
class TestCaseJpaRepositoryTest {

    private static final String TEST_CASE_NAME_1 = "TestCaseName1";

    @Autowired
    private TestCaseJpaRepository repository;

    @PersistenceContext
    EntityManager entityManager;

    @Test
    void save_thenFind_expectEntityToBePersistedSuccessfully() {
        TestCase testCase = new TestCase(TEST_CASE_NAME_1, "jiraProjectKey", "zephyrKey");

        TestCase testCaseSaved = repository.saveAndFlush(testCase);

        entityManager.detach(testCaseSaved);

        Optional<TestCase> testCaseReadOptional = repository.findByName(TEST_CASE_NAME_1);

        assertTrue(testCaseReadOptional.isPresent());
        TestCase testCaseRead = testCaseReadOptional.get();
        assertEquals(testCase.getId(), testCaseRead.getId());
    }
}
