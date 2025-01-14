package ch.admin.bit.jeap.testorchestrator.adapter.zephyr;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ZephyrWebClient {

    private final RestClient restClient;
    private final String restApiUrl;

    public ZephyrWebClient(@Value("${orchestrator.zephyr.username}") String username,
                           @Value("${orchestrator.zephyr.password}") String password,
                           @Value("${orchestrator.zephyr.restApiUrl}") String restApiUrl) {
        this.restApiUrl = restApiUrl;
        ClientHttpRequestFactory requestFactory = ClientHttpRequestFactories.get(ClientHttpRequestFactorySettings.DEFAULTS
                .withReadTimeout(Duration.ofSeconds(10)));
        this.restClient = RestClient.builder()
                .defaultHeaders(header -> header.setBasicAuth(username, password))
                .requestFactory(requestFactory)
                .requestInterceptor(this::logRequest)
                .build();
    }

    private ClientHttpResponse logRequest(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("Calling Zephyr: {} {} with headers [{}] and body {}.", request.getMethod(), request.getURI(),
                    request.getHeaders().toSingleValueMap().entrySet().stream().
                            map(entry -> entry.getKey() + ": " + (entry.getKey().equals(HttpHeaders.AUTHORIZATION) ? "*******" : entry.getValue())).
                            collect(Collectors.joining(", ")), new String(body, StandardCharsets.UTF_8));
        }
        return execution.execute(request, body);
    }

    public void testrun(ZephyrTestRunDto zephyrTestRunDto) {
        String uri = restApiUrl + "/testrun";
        log.info("Posting to Zephyr at {}: {}", uri, zephyrTestRunDto);
        restClient.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .body(zephyrTestRunDto)
                .retrieve()
                .toBodilessEntity();
    }

}

