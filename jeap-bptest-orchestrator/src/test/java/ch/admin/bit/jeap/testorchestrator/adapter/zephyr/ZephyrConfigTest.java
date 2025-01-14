package ch.admin.bit.jeap.testorchestrator.adapter.zephyr;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringJUnitConfig(initializers = ConfigDataApplicationContextInitializer.class)
@EnableConfigurationProperties(value = ZephyrConfig.class)
@ActiveProfiles("test")
class ZephyrConfigTest {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
            @Autowired
    private ZephyrConfig zephyrConfig;

    @Test
    void testZeyprConfig() {
        String restApiUrl = zephyrConfig.getRestApiUrl();
        String username = zephyrConfig.getUsername();
        String pwd = zephyrConfig.getPassword();
        String env = zephyrConfig.getZephyrEnvironment();
        assertEquals("http://localhost:", restApiUrl);
        assertEquals("theUserName", username);
        assertEquals("thePwd", pwd);
        assertEquals("LOCAL", env);


    }

}
