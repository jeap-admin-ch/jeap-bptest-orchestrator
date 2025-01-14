package ch.admin.bit.jeap.testorchestrator.adapter.zephyr;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SpringJUnitConfig(initializers = ConfigDataApplicationContextInitializer.class)
@EnableConfigurationProperties(value = ZephyrConfig.class)
@ActiveProfiles("test")
class ZephyrWebClientTest {

    private static WireMockServer wireMockServer;

    private ZephyrWebClient zephyrWebClient;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    ZephyrConfig zephyrConfig;

    @BeforeAll
    static void setUp() {
        wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
        wireMockServer.start();
    }

    @AfterAll
    static void tearDown() {
        wireMockServer.shutdown();
    }

    @BeforeEach
    void initialize() {
        String testURL = zephyrConfig.getRestApiUrl() + wireMockServer.port();
        this.zephyrWebClient = new ZephyrWebClient(zephyrConfig.getUsername(), zephyrConfig.getPassword(), testURL);
    }

    @Test
    void sendTestRun() {

        wireMockServer.stubFor(post(urlPathEqualTo("/testrun"))
                .willReturn(aResponse()
                        .withStatus(200)));

        ZephyrItemDto zephyrItem_1 = ZephyrItemDto.builder()
                .comment("blabla")
                .environment("env")
                .testCaseKey("key")
                .status("Pass")
                .scriptResult(ZephyrStepDto.builder()
                        .comment("comment")
                        .status("Pass")
                        .build())
                .build();
        ZephyrTestRunDto mockZephyrTestRunDto = ZephyrTestRunDto.builder()
                .name("Name")
                .items(zephyrItem_1)
                .projectKey("Pkey")
                .build();
        zephyrWebClient.testrun(mockZephyrTestRunDto);

        wireMockServer.verify(
                postRequestedFor(urlPathEqualTo("/testrun"))
                        .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE)));
    }
}
