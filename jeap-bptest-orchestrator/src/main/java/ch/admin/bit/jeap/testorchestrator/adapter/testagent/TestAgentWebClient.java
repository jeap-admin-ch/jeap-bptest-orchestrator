package ch.admin.bit.jeap.testorchestrator.adapter.testagent;

import ch.admin.bit.jeap.testagent.api.act.ActionDto;
import ch.admin.bit.jeap.testagent.api.act.ActionResultDto;
import ch.admin.bit.jeap.testagent.api.prepare.PreparationDto;
import ch.admin.bit.jeap.testagent.api.prepare.PreparationResultDto;
import ch.admin.bit.jeap.testagent.api.update.DynamicDataDto;
import ch.admin.bit.jeap.testagent.api.verify.ReportDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.RequestHeadersSpec.ConvertibleClientHttpResponse;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

import static net.logstash.logback.argument.StructuredArguments.value;

@Component
@Slf4j
public class TestAgentWebClient {

    private final RestClient restClient;
    private final Map<String, String> testAgentsMap;
    private final Duration readTimeout;
    private static final String API_PATH = "/api/tests/";
    private static final int TIMEOUT_5_SECONDS = 5;

    public static final String IS_4_XX_ERROR = "is4xxClientError; TestAgent: ";
    public static final String IS_5_XX_ERROR = "is5xxServerError; TestAgent: ";
    public static final String ERR_MSG = "Could not connect or timeout to TestAgent: ";

    public TestAgentWebClient(RestClient.Builder restClientBuilder, TestAgentsConfig testAgentsConfig) {
        this.testAgentsMap = testAgentsConfig.getTestAgentURLs();
        this.readTimeout = Duration.ofSeconds(testAgentsConfig.getReadTimeout(TIMEOUT_5_SECONDS));
        this.restClient = createRestClient(restClientBuilder);
    }

    private RestClient createRestClient(RestClient.Builder restClientBuilder) {
        ClientHttpRequestFactory timeoutRequestFactory = ClientHttpRequestFactories.get(ClientHttpRequestFactorySettings.DEFAULTS
                .withReadTimeout(readTimeout));
        return restClientBuilder.requestFactory(timeoutRequestFactory).build();
    }

    public PreparationResultDto prepare(String testAgentName, String testId, PreparationDto preparationDto) {
        String testAgentURL = testAgentsMap.get(testAgentName);
        log.info("Prepare {} for test {}: Put {} with url {}", value("testAgentName", testAgentName), value("testId", testId), preparationDto, testAgentURL);
        try {
            return restClient.put()
                    .uri(testAgentURL + API_PATH + testId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(preparationDto)
                    .exchange( (clientRequest, clientResponse) -> {
                        handleError(clientResponse, testId, testAgentName, testAgentURL);
                        return clientResponse.bodyTo(PreparationResultDto.class);
                    });
        } catch (Exception e) {
            throw mapException(e, testId, testAgentName, testAgentURL);
        }
    }

    public ActionResultDto act(String testAgentName, String testId, ActionDto actionDto) {
        String testAgentURL = testAgentsMap.get(testAgentName);
        log.info("Act on {} for test {}: Put {} with url {}", value("testAgentName", testAgentName), value("testId", testId), actionDto, testAgentURL);
        try {
            return restClient.post()
                    .uri(testAgentURL + API_PATH + testId + "/actions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(actionDto)
                    .exchange( (clientRequest, clientResponse) -> {
                        handleError(clientResponse, testId, testAgentName, testAgentURL);
                        return clientResponse.bodyTo(ActionResultDto.class);
                    });
        } catch (Exception e) {
            throw mapException(e, testId, testAgentName, testAgentURL);
        }
    }

    public void update(String testAgentName, String testId, DynamicDataDto dynamicDataDto) {
        String testAgentURL = testAgentsMap.get(testAgentName);
        log.info("Update {} for test {}: Put {} with url {}", value("testAgentName", testAgentName), value("testId", testId), dynamicDataDto, testAgentURL);
        try {
            restClient.put()
                    .uri(testAgentURL + API_PATH + testId + "/dynamicdata")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(dynamicDataDto)
                    .exchange( (clientRequest, clientResponse) -> {
                        handleError(clientResponse, testId, testAgentName, testAgentURL);
                        return clientResponse.bodyTo(Void.class);
                    });
        } catch (Exception e) {
            throw mapException(e, testId, testAgentName, testAgentURL);
        }
    }

    public ReportDto verify(String testAgentName, String testId) {
        String testAgentURL = testAgentsMap.get(testAgentName);
        log.info("Verify {} for test {} with url {}", value("testAgentName", testAgentName), value("testId", testId), testAgentURL);
        try {
            return restClient.get()
                    .uri(testAgentURL + API_PATH + testId + "/report")
                    .exchange( (clientRequest, clientResponse) -> {
                        handleError(clientResponse, testId, testAgentName, testAgentURL);
                        return clientResponse.bodyTo(ReportDto.class);
                    });
        } catch (Exception e) {
            throw mapException(e, testId, testAgentName, testAgentURL);
        }
    }

    public void delete(String testAgentName, String testId) {
        String testAgentURL = testAgentsMap.get(testAgentName);
        log.info("Delete on {} for test {} with url {}", value("testAgentName", testAgentName), value("testId", testId), testAgentURL);
        try {
            restClient.delete()
                    .uri(testAgentURL + API_PATH + testId)
                    .exchange( (clientRequest, clientResponse) -> {
                        handleError(clientResponse, testId, testAgentName, testAgentURL);
                        return clientResponse.bodyTo(Void.class);
                   });
        } catch (Exception e) {
            throw mapException(e, testId, testAgentName, testAgentURL);
        }
    }

    private void handleError(ConvertibleClientHttpResponse clientHttpResponse, String testId, String testAgentName, String testAgentURL) throws IOException {
        HttpStatusCode httpStatusCode = clientHttpResponse.getStatusCode();
        if (httpStatusCode.is4xxClientError()) {
            throw new TestAgentException(testId, testAgentName, testAgentURL, IS_4_XX_ERROR + testAgentName);
        } else if (httpStatusCode.is5xxServerError()) {
            throw new TestAgentException(testId, testAgentName, testAgentURL, IS_5_XX_ERROR + testAgentName);
        }
    }

    private RuntimeException mapException(Exception e, String testId, String testAgentName, String testAgentURL) {
        if (e instanceof TestAgentException tae) {
            throw tae;
        }
        return new TestAgentException(testId, testAgentName, testAgentURL, ERR_MSG + testAgentName);
    }

}

