package ch.admin.bit.jeap.testorchestrator.adapter.testagent;

import ch.admin.bit.jeap.testagent.api.act.ActionDto;
import ch.admin.bit.jeap.testagent.api.act.ActionResultDto;
import ch.admin.bit.jeap.testagent.api.prepare.PreparationDto;
import ch.admin.bit.jeap.testagent.api.prepare.PreparationResultDto;
import ch.admin.bit.jeap.testagent.api.update.DynamicDataDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

class TestAgentWebClientTest {

    private static final String TEST_AGENT_A = "TestAgentA";
    private static final String LOCALHOST_URL = "http://localhost:";
    private static final String TEST_ID_123 = "123";
    private static final String TEST_ID_678 = "678";
    private static final String API_TESTS_123 = "/api/tests/123";
    private static final String API_TESTS_123_ACTIONS = "/api/tests/123/actions";
    private static final String API_TESTS_678_DYNAMIC_DATA = "/api/tests/678/dynamicdata";
    private static final String API_TESTS_678 = "/api/tests/678";
    private static final String ERROR_4XX = "is4xxClientError";
    private static final String ERROR_5XX = "is5xxServerError";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static WireMockServer wireMockServer;

    private TestAgentWebClient testAgentWebClient;

    @BeforeAll
    static void startWiremock() {
        wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
        wireMockServer.start();
    }

    @AfterAll
    static void stopWiremock() {
        wireMockServer.stop();
    }

    @BeforeEach
    void initialize() {
        RestClient.Builder restClientBuilder = RestClient.builder();
        Map<String, String> testAgentUrls = Map.of(TEST_AGENT_A, LOCALHOST_URL + wireMockServer.port());
        TestAgentsConfig testAgentsConfig = new TestAgentsConfig(testAgentUrls);
        testAgentWebClient = new TestAgentWebClient(restClientBuilder, testAgentsConfig);
    }

    @Test
    void prepare() throws Exception {
        PreparationResultDto mockPreparationResultDto = PreparationResultDto.builder()
                .data("dataKey", "dataValue")
                .build();
        wireMockServer.stubFor(put(urlPathEqualTo(API_TESTS_123))
                .willReturn(aResponse()
                        .withBody(MAPPER.writeValueAsString(mockPreparationResultDto))
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));

        PreparationDto mockPreparationDto = PreparationDto.builder()
                .testCase("TC")
                .data("key1", "value1")
                .callbackBaseUrl("URL")
                .build();
        PreparationResultDto preparationResultDto = testAgentWebClient.prepare(TEST_AGENT_A, TEST_ID_123, mockPreparationDto);

        assertEquals(mockPreparationResultDto.getData(), preparationResultDto.getData());

        wireMockServer.verify(
                putRequestedFor(urlPathEqualTo(API_TESTS_123))
                        .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE)));
    }

    @Test
    void prepareMustThrowExceptions() throws Exception {
        PreparationResultDto mockPreparationResultDto = PreparationResultDto.builder()
                .data("dataKey", "dataValue")
                .build();
        wireMockServer.stubFor(put(urlPathEqualTo(API_TESTS_123))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withFixedDelay(10000))); // force timeout

        PreparationDto mockPreparationDto = PreparationDto.builder()
                .testCase("TC")
                .data("key1", "value1")
                .callbackBaseUrl("URL")
                .build();

        //Test 1: Timeout
        TestAgentException exception = assertThrows(TestAgentException.class, () -> testAgentWebClient.prepare(TEST_AGENT_A, TEST_ID_123, mockPreparationDto));

        assertTrue(exception.getMessage().contains(TEST_AGENT_A));
        assertEquals(TEST_ID_123, exception.getTestId());

        // Test 2: 4xx - Response
        wireMockServer.stubFor(put(urlPathEqualTo(API_TESTS_123))
                .willReturn(aResponse()
                        .withBody(MAPPER.writeValueAsString(mockPreparationResultDto))
                        .withStatus(400)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));
        exception = assertThrows(TestAgentException.class, () -> testAgentWebClient.prepare(TEST_AGENT_A, TEST_ID_123, mockPreparationDto));

        assertTrue(exception.getMessage().contains(ERROR_4XX));
        assertEquals(TEST_ID_123, exception.getTestId());

        // Test 3: 5xx - Response
        wireMockServer.stubFor(put(urlPathEqualTo(API_TESTS_123))
                .willReturn(aResponse()
                        .withBody(MAPPER.writeValueAsString(mockPreparationResultDto))
                        .withStatus(500)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));
        exception = assertThrows(TestAgentException.class, () -> testAgentWebClient.prepare(TEST_AGENT_A, TEST_ID_123, mockPreparationDto));

        assertTrue(exception.getMessage().contains(ERROR_5XX));
        assertEquals(TEST_ID_123, exception.getTestId());

        wireMockServer.verify(
                putRequestedFor(urlPathEqualTo(API_TESTS_123))
                        .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE)));
    }

    @Test
    void act() throws Exception {

        ActionResultDto mockActionResultDto = ActionResultDto.builder()
                .data("dataKey", "dataValue")
                .build();
        wireMockServer.stubFor(post(urlPathEqualTo(API_TESTS_123_ACTIONS))
                .willReturn(aResponse()
                        .withBody(MAPPER.writeValueAsString(mockActionResultDto))
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));

        ActionDto actionDto = ActionDto.builder()
                .action("Action")
                .data(Map.of("key1", "value1"))
                .build();
        ActionResultDto actionResultDto = testAgentWebClient.act(TEST_AGENT_A, TEST_ID_123, actionDto);

        assertEquals(mockActionResultDto.getData(), actionResultDto.getData());

        wireMockServer.verify(
                postRequestedFor(urlPathEqualTo(API_TESTS_123_ACTIONS))
                        .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE)));
    }

    @Test
    void actMustThrowExceptions() {

        wireMockServer.stubFor(post(urlPathEqualTo(API_TESTS_123_ACTIONS))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withFixedDelay(10000))); // force timeout

        ActionDto actionDto = ActionDto.builder()
                .action("Action")
                .data(Map.of("key1", "value1"))
                .build();

        //Test 1: Timeout
        TestAgentException exception = assertThrows(TestAgentException.class, () ->
                testAgentWebClient.act(TEST_AGENT_A, TEST_ID_123, actionDto));

        assertTrue(exception.getMessage().contains(TEST_AGENT_A));
        assertEquals(TEST_ID_123, exception.getTestId());
        assertEquals(TEST_AGENT_A, exception.getTestAgentName());
        assertEquals(LOCALHOST_URL + wireMockServer.port(), exception.getRequestUrl());

        // Test 2: 4xx - Response
        wireMockServer.stubFor(post(urlPathEqualTo(API_TESTS_123_ACTIONS))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));
        exception = assertThrows(TestAgentException.class, () ->
                testAgentWebClient.act(TEST_AGENT_A, TEST_ID_123, actionDto));

        assertTrue(exception.getMessage().contains(ERROR_4XX));
        assertEquals(TEST_ID_123, exception.getTestId());
        assertEquals(TEST_AGENT_A, exception.getTestAgentName());
        assertEquals(LOCALHOST_URL + wireMockServer.port(), exception.getRequestUrl());

        // Test 3: 5xx - Response
        wireMockServer.stubFor(post(urlPathEqualTo(API_TESTS_123_ACTIONS))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));
        exception = assertThrows(TestAgentException.class, () ->
                testAgentWebClient.act(TEST_AGENT_A, TEST_ID_123, actionDto));

        assertTrue(exception.getMessage().contains(ERROR_5XX));
        assertEquals(TEST_ID_123, exception.getTestId());
        assertEquals(TEST_AGENT_A, exception.getTestAgentName());
        assertEquals(LOCALHOST_URL + wireMockServer.port(), exception.getRequestUrl());
    }

    @Test
    void update() {

        wireMockServer.stubFor(put(urlPathEqualTo(API_TESTS_678_DYNAMIC_DATA))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));

        DynamicDataDto dynamicDataDto = new DynamicDataDto(Map.of("key1", "value1"));
        testAgentWebClient.update(TEST_AGENT_A, TEST_ID_678, dynamicDataDto);

        wireMockServer.verify(
                putRequestedFor(urlPathEqualTo(API_TESTS_678_DYNAMIC_DATA))
                        .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE)));
    }

    @Test
    void updateMustThrowExceptions() {

        wireMockServer.stubFor(put(urlPathEqualTo(API_TESTS_678_DYNAMIC_DATA))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withFixedDelay(10000))); // force timeout

        DynamicDataDto dynamicDataDto = new DynamicDataDto(Map.of("key1", "value1"));

        //Test 1: Timeout
        TestAgentException exception = assertThrows(TestAgentException.class, () -> testAgentWebClient.update(TEST_AGENT_A, TEST_ID_678, dynamicDataDto));

        assertTrue(exception.getMessage().contains(TEST_AGENT_A));
        assertEquals(TEST_ID_678, exception.getTestId());

        // Test 2: 4xx - Response
        wireMockServer.stubFor(put(urlPathEqualTo(API_TESTS_678_DYNAMIC_DATA))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));
        exception = assertThrows(TestAgentException.class, () -> testAgentWebClient.update(TEST_AGENT_A, TEST_ID_678, dynamicDataDto));

        assertTrue(exception.getMessage().contains(ERROR_4XX));
        assertEquals(TEST_ID_678, exception.getTestId());

        // Test 3: 5xx - Response
        wireMockServer.stubFor(put(urlPathEqualTo(API_TESTS_678_DYNAMIC_DATA))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));
        exception = assertThrows(TestAgentException.class, () -> testAgentWebClient.update(TEST_AGENT_A, TEST_ID_678, dynamicDataDto));

        assertTrue(exception.getMessage().contains(ERROR_5XX));
        assertEquals(TEST_ID_678, exception.getTestId());
    }

    @Test
    void deleteOk() {

        wireMockServer.stubFor(delete(urlPathEqualTo(API_TESTS_678))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));

        testAgentWebClient.delete(TEST_AGENT_A, TEST_ID_678);

        wireMockServer.verify(
                deleteRequestedFor(urlPathEqualTo(API_TESTS_678)));
    }

    @Test
    void deleteMustThrowExceptions() {

        wireMockServer.stubFor(delete(urlPathEqualTo(API_TESTS_678))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withFixedDelay(10000))); // force timeout

        //Test 1: Timeout
        TestAgentException exception = assertThrows(TestAgentException.class, () -> testAgentWebClient.delete(TEST_AGENT_A, TEST_ID_678));
        assertTrue(exception.getMessage().contains(TEST_AGENT_A));
        assertEquals(TEST_ID_678, exception.getTestId());

        // Test 2: 4xx - Response
        wireMockServer.stubFor(delete(urlPathEqualTo(API_TESTS_678))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));
        exception = assertThrows(TestAgentException.class, () -> testAgentWebClient.delete(TEST_AGENT_A, TEST_ID_678));
        assertTrue(exception.getMessage().contains(ERROR_4XX));
        assertEquals(TEST_ID_678, exception.getTestId());

        // Test 3: 5xx - Response
        wireMockServer.stubFor(delete(urlPathEqualTo(API_TESTS_678))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));
        exception = assertThrows(TestAgentException.class, () -> testAgentWebClient.delete(TEST_AGENT_A, TEST_ID_678));
        assertTrue(exception.getMessage().contains(ERROR_5XX));
        assertEquals(TEST_ID_678, exception.getTestId());
    }
}
