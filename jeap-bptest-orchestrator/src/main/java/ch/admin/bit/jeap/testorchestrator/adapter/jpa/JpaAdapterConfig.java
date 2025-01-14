package ch.admin.bit.jeap.testorchestrator.adapter.jpa;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@AutoConfiguration
@EnableTransactionManagement
@EnableJpaRepositories
@EnableJpaAuditing
@ComponentScan
@EntityScan(basePackages = "ch.admin.bit.jeap.testorchestrator")
@PropertySource("classpath:jpaDefaultProperties.properties")
class JpaAdapterConfig {

}
