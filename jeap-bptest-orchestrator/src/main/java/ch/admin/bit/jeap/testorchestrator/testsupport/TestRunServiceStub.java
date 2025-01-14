package ch.admin.bit.jeap.testorchestrator.testsupport;

import ch.admin.bit.jeap.testorchestrator.services.TestRunService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestRunServiceStub extends TestRunService {
    private final String currentTestRunId;
    private final Map<String, String> params = new HashMap<>();

    TestRunServiceStub(String currentTestRunId) {
        super(null, null, null);
        this.currentTestRunId = currentTestRunId;
    }

    @Override
    public String getParameterValue(String testId, String key) {
        assertEquals(currentTestRunId, testId);
        return params.get(key);
    }

    @Override
    public Map<String, String> getParameters(String testId) {
        assertEquals(currentTestRunId, testId);
        return Map.copyOf(params);
    }

    @Override
    public void setParameter(String testId, String key, String value) {
        assertEquals(currentTestRunId, testId);
        params.put(key, value);
    }
}
