package ch.admin.bit.jeap.testorchestrator.adapter.zephyr;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "orchestrator.zephyr")
@Data
public class ZephyrConfig {

    private String restApiUrl;

    private String username;

    private String password;

    private String zephyrEnvironment;

}
