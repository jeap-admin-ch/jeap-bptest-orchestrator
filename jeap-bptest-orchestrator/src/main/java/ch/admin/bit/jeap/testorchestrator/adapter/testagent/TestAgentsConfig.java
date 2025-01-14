package ch.admin.bit.jeap.testorchestrator.adapter.testagent;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "orchestrator")
public class TestAgentsConfig {

    private final Map<String, String> testAgentURLs;
    @Setter
    private Integer readTimeout;

    public TestAgentsConfig(Map<String, String> testAgentURLs) {
        this.testAgentURLs = testAgentURLs;
    }

    public Map<String, String> getTestAgentURLs() {
        return testAgentURLs;
    }

    public int getReadTimeout(int defaultValue) {
        return readTimeout != null ? readTimeout : defaultValue;
    }
}
