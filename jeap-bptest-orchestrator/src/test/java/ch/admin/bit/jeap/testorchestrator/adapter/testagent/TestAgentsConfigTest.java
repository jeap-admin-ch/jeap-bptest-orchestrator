package ch.admin.bit.jeap.testorchestrator.adapter.testagent;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringJUnitConfig(initializers = ConfigDataApplicationContextInitializer.class)
@EnableConfigurationProperties(value = TestAgentsConfig.class)
@ActiveProfiles("test")
class TestAgentsConfigTest {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
            @Autowired
    private TestAgentsConfig testAgentsConfig;

    @Test
    void testAgentUrlMap() {
        Map<String, String> testAgentUrlMap = testAgentsConfig.getTestAgentURLs();
        assertEquals(2, testAgentUrlMap.size());
        String urlA = testAgentUrlMap.get("TestAgentA");
        assertEquals("urlA", urlA);
        String urlB = testAgentUrlMap.get("TestAgentB");
        assertEquals("urlB", urlB);
    }
}
