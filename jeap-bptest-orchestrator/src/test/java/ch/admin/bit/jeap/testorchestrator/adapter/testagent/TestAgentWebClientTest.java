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

    private final static String TEST_AGENT_A = "TestAgentA";
    private final static ObjectMapper MAPPER = new ObjectMapper();

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
        Map<String, String> testAgentUrls = Map.of(TEST_AGENT_A, "http://localhost:" + wireMockServer.port());
        TestAgentsConfig testAgentsConfig = new TestAgentsConfig(testAgentUrls);
        testAgentWebClient = new TestAgentWebClient(restClientBuilder, testAgentsConfig);
    }

    @Test
    void prepare() throws Exception {
        PreparationResultDto mockPreparationResultDto = PreparationResultDto.builder()
                .data("dataKey", "dataValue")
                .build();
        wireMockServer.stubFor(put(urlPathEqualTo("/api/tests/123"))
                .willReturn(aResponse()
                        .withBody(MAPPER.writeValueAsString(mockPreparationResultDto))
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));

        PreparationDto mockPreparationDto = PreparationDto.builder()
                .testCase("TC")
                .data("key1", "value1")
                .callbackBaseUrl("URL")
                .build();
        PreparationResultDto preparationResultDto = testAgentWebClient.prepare(TEST_AGENT_A, "123", mockPreparationDto);

        assertEquals(mockPreparationResultDto.getData(), preparationResultDto.getData());

        wireMockServer.verify(
                putRequestedFor(urlPathEqualTo("/api/tests/123"))
                        .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE)));
    }

    @Test
    void prepareMustThrowExceptions() throws Exception {
        PreparationResultDto mockPreparationResultDto = PreparationResultDto.builder()
                .data("dataKey", "dataValue")
                .build();
        wireMockServer.stubFor(put(urlPathEqualTo("/api/tests/123"))
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
        TestAgentException exception = assertThrows(TestAgentException.class, () -> testAgentWebClient.prepare(TEST_AGENT_A, "123", mockPreparationDto));

        assertTrue(exception.getMessage().contains(TEST_AGENT_A));
        assertEquals("123", exception.getTestId());

        // Test 2: 4xx - Response
        wireMockServer.stubFor(put(urlPathEqualTo("/api/tests/123"))
                .willReturn(aResponse()
                        .withBody(MAPPER.writeValueAsString(mockPreparationResultDto))
                        .withStatus(400)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));
        exception = assertThrows(TestAgentException.class, () -> testAgentWebClient.prepare(TEST_AGENT_A, "123", mockPreparationDto));

        assertTrue(exception.getMessage().contains("is4xxClientError"));
        assertEquals("123", exception.getTestId());

        // Test 3: 5xx - Response
        wireMockServer.stubFor(put(urlPathEqualTo("/api/tests/123"))
                .willReturn(aResponse()
                        .withBody(MAPPER.writeValueAsString(mockPreparationResultDto))
                        .withStatus(500)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));
        exception = assertThrows(TestAgentException.class, () -> testAgentWebClient.prepare(TEST_AGENT_A, "123", mockPreparationDto));

        assertTrue(exception.getMessage().contains("is5xxServerError"));
        assertEquals("123", exception.getTestId());

        wireMockServer.verify(
                putRequestedFor(urlPathEqualTo("/api/tests/123"))
                        .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE)));
    }

    @Test
    void act() throws Exception {

        ActionResultDto mockActionResultDto = ActionResultDto.builder()
                .data("dataKey", "dataValue")
                .build();
        wireMockServer.stubFor(post(urlPathEqualTo("/api/tests/123/actions"))
                .willReturn(aResponse()
                        .withBody(MAPPER.writeValueAsString(mockActionResultDto))
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));

        ActionDto actionDto = ActionDto.builder()
                .action("Action")
                .data(Map.of("key1", "value1"))
                .build();
        ActionResultDto actionResultDto = testAgentWebClient.act(TEST_AGENT_A, "123", actionDto);

        assertEquals(mockActionResultDto.getData(), actionResultDto.getData());

        wireMockServer.verify(
                postRequestedFor(urlPathEqualTo("/api/tests/123/actions"))
                        .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE)));
    }

    @Test
    void actMustThrowExceptions() {

        wireMockServer.stubFor(post(urlPathEqualTo("/api/tests/123/actions"))
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
                testAgentWebClient.act(TEST_AGENT_A, "123", actionDto));

        assertTrue(exception.getMessage().contains(TEST_AGENT_A));
        assertEquals("123", exception.getTestId());
        assertEquals(TEST_AGENT_A, exception.getTestAgentName());
        assertEquals("http://localhost:" + wireMockServer.port(), exception.getRequestUrl());

        // Test 2: 4xx - Response
        wireMockServer.stubFor(post(urlPathEqualTo("/api/tests/123/actions"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));
        exception = assertThrows(TestAgentException.class, () ->
                testAgentWebClient.act(TEST_AGENT_A, "123", actionDto));

        assertTrue(exception.getMessage().contains("is4xxClientError"));
        assertEquals("123", exception.getTestId());
        assertEquals(TEST_AGENT_A, exception.getTestAgentName());
        assertEquals("http://localhost:" + wireMockServer.port(), exception.getRequestUrl());

        // Test 3: 5xx - Response
        wireMockServer.stubFor(post(urlPathEqualTo("/api/tests/123/actions"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));
        exception = assertThrows(TestAgentException.class, () ->
                testAgentWebClient.act(TEST_AGENT_A, "123", actionDto));

        assertTrue(exception.getMessage().contains("is5xxServerError"));
        assertEquals("123", exception.getTestId());
        assertEquals(TEST_AGENT_A, exception.getTestAgentName());
        assertEquals("http://localhost:" + wireMockServer.port(), exception.getRequestUrl());
    }

    @Test
    void update() {

        wireMockServer.stubFor(put(urlPathEqualTo("/api/tests/678/dynamicdata"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));

        DynamicDataDto dynamicDataDto = new DynamicDataDto(Map.of("key1", "value1"));
        testAgentWebClient.update(TEST_AGENT_A, "678", dynamicDataDto);

        wireMockServer.verify(
                putRequestedFor(urlPathEqualTo("/api/tests/678/dynamicdata"))
                        .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE)));
    }

    @Test
    void updateMustThrowExceptions() {

        wireMockServer.stubFor(put(urlPathEqualTo("/api/tests/678/dynamicdata"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withFixedDelay(10000))); // force timeout

        DynamicDataDto dynamicDataDto = new DynamicDataDto(Map.of("key1", "value1"));

        //Test 1: Timeout
        TestAgentException exception = assertThrows(TestAgentException.class, () -> testAgentWebClient.update(TEST_AGENT_A, "678", dynamicDataDto));

        assertTrue(exception.getMessage().contains(TEST_AGENT_A));
        assertEquals("678", exception.getTestId());

        // Test 2: 4xx - Response
        wireMockServer.stubFor(put(urlPathEqualTo("/api/tests/678/dynamicdata"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));
        exception = assertThrows(TestAgentException.class, () -> testAgentWebClient.update(TEST_AGENT_A, "678", dynamicDataDto));

        assertTrue(exception.getMessage().contains("is4xxClientError"));
        assertEquals("678", exception.getTestId());

        // Test 3: 5xx - Response
        wireMockServer.stubFor(put(urlPathEqualTo("/api/tests/678/dynamicdata"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));
        exception = assertThrows(TestAgentException.class, () -> testAgentWebClient.update(TEST_AGENT_A, "678", dynamicDataDto));

        assertTrue(exception.getMessage().contains("is5xxServerError"));
        assertEquals("678", exception.getTestId());
    }

    @Test
    void deleteOk() {

        wireMockServer.stubFor(delete(urlPathEqualTo("/api/tests/678"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));

        testAgentWebClient.delete(TEST_AGENT_A, "678");

        wireMockServer.verify(
                deleteRequestedFor(urlPathEqualTo("/api/tests/678")));
    }

    @Test
    void deleteMustThrowExceptions() {

        wireMockServer.stubFor(delete(urlPathEqualTo("/api/tests/678"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withFixedDelay(10000))); // force timeout

        //Test 1: Timeout
        TestAgentException exception = assertThrows(TestAgentException.class, () -> testAgentWebClient.delete(TEST_AGENT_A, "678"));
        assertTrue(exception.getMessage().contains(TEST_AGENT_A));
        assertEquals("678", exception.getTestId());

        // Test 2: 4xx - Response
        wireMockServer.stubFor(delete(urlPathEqualTo("/api/tests/678"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));
        exception = assertThrows(TestAgentException.class, () -> testAgentWebClient.delete(TEST_AGENT_A, "678"));
        assertTrue(exception.getMessage().contains("is4xxClientError"));
        assertEquals("678", exception.getTestId());

        // Test 3: 5xx - Response
        wireMockServer.stubFor(delete(urlPathEqualTo("/api/tests/678"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));
        exception = assertThrows(TestAgentException.class, () -> testAgentWebClient.delete(TEST_AGENT_A, "678"));
        assertTrue(exception.getMessage().contains("is5xxServerError"));
        assertEquals("678", exception.getTestId());
    }
/*
    @Test
    void verify() throws Exception {
        String testId = UUID.randomUUID().toString();

        ReportDto mockReportDto = ReportDto.builder()
                .dateTime(ZonedDateTime.now())
                .results(new ArrayList<>())
                .testId(testId)
                .testcase("TestCaseName")
                .build();
        mockBackEnd.enqueue(new MockResponse()
                .setBody(MAPPER
                        .registerModule(new JavaTimeModule())
                        .writeValueAsString(mockReportDto))
                .addHeader("Content-Type", "application/json"));

        ReportDto reportDto = testAgentWebClient.verify(TEST_AGENT_A, testId);

        assertEquals(mockReportDto.getTestId(), reportDto.getTestId());

        RecordedRequest recordedRequest = mockBackEnd.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/api/tests/" + testId + "/report", recordedRequest.getPath());
    }

    @Test
    void verifyMustThrowExceptions() throws Exception {
        String testId = UUID.randomUUID().toString();

        ReportDto mockReportDto = ReportDto.builder()
                .dateTime(ZonedDateTime.now())
                .results(new ArrayList<>())
                .testId(testId)
                .testcase("TestCaseName")
                .build();
        mockBackEnd.enqueue(new MockResponse()
                .setBody(MAPPER
                        .registerModule(new JavaTimeModule())
                        .writeValueAsString(mockReportDto))
                .addHeader("Content-Type", "application/json")
                .setBodyDelay(6, TimeUnit.SECONDS));

        //Test 1: Timeout
        TestAgentException exception = assertThrows(TestAgentException.class, () -> testAgentWebClient.verify(TEST_AGENT_A, testId));
        assertTrue(exception.getMessage().contains(TEST_AGENT_A));
        assertEquals(testId, exception.getTestId());

        // Test 2: 4xx - Response
        mockBackEnd.enqueue(new MockResponse().setResponseCode(400));
        exception = assertThrows(TestAgentException.class, () -> testAgentWebClient.verify(TEST_AGENT_A, testId));
        assertTrue(exception.getMessage().contains("is4xxClientError"));
        assertEquals(testId, exception.getTestId());

        // Test 3: 5xx - Response
        mockBackEnd.enqueue(new MockResponse().setResponseCode(500));
        exception = assertThrows(TestAgentException.class, () -> testAgentWebClient.verify(TEST_AGENT_A, testId));
        assertTrue(exception.getMessage().contains("is5xxServerError"));
        assertEquals(testId, exception.getTestId());
    }
 */
}
