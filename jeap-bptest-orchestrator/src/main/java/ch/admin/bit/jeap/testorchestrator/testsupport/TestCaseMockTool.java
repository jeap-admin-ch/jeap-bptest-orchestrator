package ch.admin.bit.jeap.testorchestrator.testsupport;

import ch.admin.bit.jeap.testagent.api.act.ActionDto;
import ch.admin.bit.jeap.testagent.api.act.ActionResultDto;
import ch.admin.bit.jeap.testagent.api.prepare.PreparationDto;
import ch.admin.bit.jeap.testagent.api.prepare.PreparationResultDto;
import ch.admin.bit.jeap.testagent.api.update.DynamicDataDto;
import ch.admin.bit.jeap.testagent.api.verify.ReportDto;
import ch.admin.bit.jeap.testorchestrator.adapter.testagent.TestAgentWebClient;
import ch.admin.bit.jeap.testorchestrator.services.TestReportService;
import lombok.Builder;
import lombok.NonNull;
import org.junit.jupiter.api.Assertions;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Support class for unit testing test cases with Mockito mocks playing the part of the test agent web client and the test report service.
 * Provides some methods to verify the common calls that test cases make to test agents and to the report service.
 * Also provides some methods to mock the behaviour of test agents in the act phase.
 */
@Builder
public class TestCaseMockTool {

    @NonNull
    private final TestAgentWebClient testAgentWebClientMock;
    @NonNull
    private final TestReportService testReportServiceMock;
    @NonNull
    private final String testId;
    @NonNull
    private final String callBackBaseUrl;

    public void mockPrepareCall(final String testAgentName, final PreparationResultDto preparationResultDto) {
        Mockito.when(testAgentWebClientMock.prepare(eq(testAgentName), eq(testId), any())).thenReturn(preparationResultDto);
    }

    public PreparationDto assertPrepareCalled(final String testAgentName, final String testCase) {
        ArgumentCaptor<PreparationDto> preparationDtoCaptor = ArgumentCaptor.forClass(PreparationDto.class);
        verify(testAgentWebClientMock).prepare(eq(testAgentName), eq(testId), preparationDtoCaptor.capture());
        PreparationDto preparationDto = preparationDtoCaptor.getValue();
        assertNotNull(preparationDto);
        assertEquals(testCase, preparationDto.getTestCase());
        assertEquals(callBackBaseUrl, preparationDto.getCallbackBaseUrl());
        return preparationDto;
    }

    public void mockActCall(final String testAgentName, Supplier<ActionResultDto> act) {
        Mockito.doAnswer(invocation -> act.get())
        .when(testAgentWebClientMock).act(eq(testAgentName), eq(testId), any());
    }

    public void mockActCall(final String testAgentName, ActionDto actionDto, Supplier<ActionResultDto> act) {
        Mockito.doAnswer(invocation -> act.get())
        .when(testAgentWebClientMock).act(eq(testAgentName), eq(testId), eq(actionDto));
    }

    public ActionDto assertActCalled(final String testAgentName, final String action) {
        ArgumentCaptor<ActionDto> actionDtoCaptor = ArgumentCaptor.forClass(ActionDto.class);
        verify(testAgentWebClientMock).act(eq(testAgentName), eq(testId), actionDtoCaptor.capture());
        ActionDto actionDto = actionDtoCaptor.getValue();
        assertNotNull(actionDto);
        assertEquals(action, actionDto.getAction());
        return actionDto;
    }

    /**
     * If there are several Actions which call act, you must use this for Assertion
     * @param testAgentName the Name of the TestAgent
     * @param actions can be more than one
     * @return A List of ActionDto's
     */
    public List<ActionDto> assertActsCalled(String testAgentName, String... actions) {
        ArgumentCaptor<ActionDto> actionDtoCaptor = ArgumentCaptor.forClass(ActionDto.class);
        Mockito.verify(this.testAgentWebClientMock, Mockito.times(actions.length))
                .act(ArgumentMatchers.eq(testAgentName), ArgumentMatchers.eq(this.testId), actionDtoCaptor.capture());

        List<ActionDto> capturedActionDtos = actionDtoCaptor.getAllValues();
        capturedActionDtos.forEach(Assertions::assertNotNull);
        Set<String> capturedActions = capturedActionDtos.stream().map(ActionDto::getAction).collect(Collectors.toSet());

        Set<String> actionSet = new HashSet<>(Arrays.asList(actions));
        assertEquals(actionSet, capturedActions);
        return capturedActionDtos;
    }

    public DynamicDataDto assertUpdateCalled(final String testAgentName) {
        ArgumentCaptor<DynamicDataDto> dynamicDataDtoCaptor = ArgumentCaptor.forClass(DynamicDataDto.class);
        verify(testAgentWebClientMock).update(eq(testAgentName), eq(testId), dynamicDataDtoCaptor.capture());
        DynamicDataDto dynamicDataDto = dynamicDataDtoCaptor.getValue();
        assertNotNull(dynamicDataDto);
        return dynamicDataDto;
    }

    public void assertVerifyCalled(final String... testAgentNames) {
        Arrays.stream(testAgentNames).forEach( testAgentName ->
                verify(testAgentWebClientMock).verify(eq(testAgentName), eq(testId))
        );
    }

    public List<ReportDto> assertPersistTestResultCalled(int times) {
        ArgumentCaptor<ReportDto> reportDtoCaptor = ArgumentCaptor.forClass(ReportDto.class);
        verify(testReportServiceMock, Mockito.times(times)).persistTestResult(eq(testId), reportDtoCaptor.capture());
        return reportDtoCaptor.getAllValues();
    }

    public void assertDeleteCalled(final String... testAgentNames) {
        Arrays.stream(testAgentNames).forEach( testAgentName ->
                verify(testAgentWebClientMock).delete(eq(testAgentName), eq(testId))
        );
    }

    public void assertNoMoreTestAgentWebClientInteractions() {
        Mockito.verifyNoMoreInteractions(testAgentWebClientMock);
    }

    public void assertNoMoreTestReportServiceInteractions() {
        Mockito.verifyNoMoreInteractions(testReportServiceMock);
    }

}
