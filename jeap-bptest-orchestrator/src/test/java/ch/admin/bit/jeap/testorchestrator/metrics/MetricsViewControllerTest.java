package ch.admin.bit.jeap.testorchestrator.metrics;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.AllOf.allOf;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({MetricTestConfig.class})
@ContextConfiguration(classes= {MetricsConfig.class})
class MetricsViewControllerTest {

    private static final String TEST_CASE_NAME_1 = "MyTestCase_1";
    private static final String TEST_CASE_NAME_2 = "MyTestCase_2";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TestCaseMetricsService testCaseMetricsService;

    @SneakyThrows
    @Test
    void renderMetrics() {
        doReturn(createMetricList()).when(testCaseMetricsService).getAllTestCaseMetrics();

        mockMvc.perform(get("/metrics"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(allOf(
                        containsString("<html"),
                        containsString(TEST_CASE_NAME_1),
                        containsString(TEST_CASE_NAME_2),
                        containsString("299"),
                        containsString("198"),
                        containsString("1"),
                        containsString("2"))));
    }

    @SneakyThrows
    @Test
    void should_redirect_root_to_metrics() {
        mockMvc.perform(get("/"))
                .andExpect(status().isFound())
                .andExpect(header().string(HttpHeaders.LOCATION, "/metrics"));
    }

    private List<TestCaseMetricsDto> createMetricList() {
        TestCaseMetricsDto testCaseMetricsDto_1 = new TestCaseMetricsDto(TEST_CASE_NAME_1,
                199,
                198,
                1,
                "00:01:30.00",
                "00:00:15.00");

        TestCaseMetricsDto testCaseMetricsDto_2 = new TestCaseMetricsDto(TEST_CASE_NAME_2,
                299,
                298,
                2,
                "00:01:30.00",
                "00:00:15.00");

        return List.of(testCaseMetricsDto_1, testCaseMetricsDto_2);
    }

}
