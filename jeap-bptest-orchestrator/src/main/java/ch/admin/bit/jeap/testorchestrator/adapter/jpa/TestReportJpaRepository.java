package ch.admin.bit.jeap.testorchestrator.adapter.jpa;


import ch.admin.bit.jeap.testorchestrator.domain.TestReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TestReportJpaRepository extends JpaRepository<TestReport, UUID> {

 }
