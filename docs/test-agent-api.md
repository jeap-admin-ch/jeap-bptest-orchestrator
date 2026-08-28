# TestAgent API contract

The orchestrator talks to application-specific test agents through the separate `jeap-bptestagent-api` library. This
repository does not define that API, but it depends on it and uses its DTOs and operation shapes.

## Operations

Test agents are expected to provide the following REST contract under `/api/tests`:

```java
@RequestMapping("/api/tests")
public interface TestAgentOperations {

    @PutMapping("/{testId}")
    ResponseEntity<PreparationResultDto> prepare(@PathVariable String testId,
                                                 @RequestBody PreparationDto preparationDto);

    @PutMapping("/{testId}/dynamicdata")
    void update(@PathVariable String testId,
                @RequestBody DynamicDataDto dynamicDataDto);

    @PostMapping("/{testId}/actions")
    ResponseEntity<ActionResultDto> act(@PathVariable String testId,
                                        @RequestBody ActionDto actionDto);

    @GetMapping("/{testId}/report")
    ResponseEntity<ReportDto> verify(@PathVariable String testId);

    @DeleteMapping("/{testId}")
    void cleanUp(@PathVariable String testId);
}
```

## Semantics

| Operation | Endpoint | Purpose |
|---|---|---|
| Prepare | `PUT /api/tests/{testId}` | Prepare the application for a test run. The request contains the callback base URL, test-case name, and optional initial data. The response may return data needed by other participants. |
| Update | `PUT /api/tests/{testId}/dynamicdata` | Push dynamic key/value data that only became available after the test started. |
| Act | `POST /api/tests/{testId}/actions` | Trigger an action known to the test agent, optionally with key/value parameters. |
| Verify | `GET /api/tests/{testId}/report` | Return a verification report describing whether expectations were met. |
| Clean up | `DELETE /api/tests/{testId}` | Delete test data created for the run. |

## Orchestrator-side usage

`TestAgentWebClient` wraps these operations for orchestrator test cases:

- `prepare(...)`
- `act(...)`
- `update(...)`
- `verify(...)`
- `delete(...)`

Logical test-agent names such as `OrderTestAgent` are resolved through `orchestrator.testagentURLs`.

## Related pages

- [Architecture](architecture.md)
- [Configuration reference](configuration.md)
- [Test-case lifecycle](test-case-lifecycle.md)
