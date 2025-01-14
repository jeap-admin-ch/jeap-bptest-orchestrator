package ch.admin.bit.jeap.testorchestrator.adapter.jpa;

import ch.admin.bit.jeap.testorchestrator.domain.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TestCaseJpaRepository extends JpaRepository<TestCase, UUID> {
     Optional<TestCase> findByName(String testCaseName);
 }
