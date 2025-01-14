package ch.admin.bit.jeap.testorchestrator.adapter.jpa;

import ch.admin.bit.jeap.testorchestrator.domain.TestCase;
import ch.admin.bit.jeap.testorchestrator.domain.TestRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TestRunJpaRepository extends JpaRepository<TestRun, UUID> {

    List<TestRun> findByTestCase(TestCase testCase);

 }
