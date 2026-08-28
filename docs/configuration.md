# Configuration

The library uses the `orchestrator.*` configuration namespace. Some properties are bound through
`@ConfigurationProperties`, others are injected directly with `@Value`.

## Property reference

| Property | Required | Default | Purpose |
|---|---:|---|---|
| `orchestrator.callbackUrl` | yes | none | Callback base URL put into `PreparationDto.callbackBaseUrl` so test agents know where to send logs and notifications. |
| `orchestrator.testRunTimeout` | no | `30000` | Maximum test-run duration in milliseconds before `TestCaseService` aborts a still-running test. |
| `orchestrator.readTimeout` | no | `5` | Read timeout in seconds for calls from `TestAgentWebClient` to test agents. |
| `orchestrator.testagentURLs.<name>` | yes | none | Map of logical test-agent name to base URL. Accessed via `TestAgentsConfig.getTestAgentURLs()`. |
| `orchestrator.testAgent.apiPath` | no | `/api/tests/` | API path appended between the test-agent base URL and test id. Leading/trailing slashes are normalized. |
| `orchestrator.zephyr.restApiUrl` | yes | none | Base URL of the Zephyr REST API. |
| `orchestrator.zephyr.username` | yes | none | Username used for Basic authentication against Zephyr. |
| `orchestrator.zephyr.password` | yes | none | Password used for Basic authentication against Zephyr. |
| `orchestrator.zephyr.zephyrEnvironment` | yes | none | Environment name stored on new `TestRun`s and sent to Zephyr. |
| `orchestrator.zephyr.testRunPath` | no | `/testrun` | Path appended to `restApiUrl` when posting test-run results. |

## Example

```yaml
orchestrator:
  callbackUrl: http://localhost:8300/example-orchestrator
  testRunTimeout: 30000
  readTimeout: 5
  testagentURLs:
    OrderTestAgent: http://localhost:8271/order-testagent
    BillingTestAgent: http://localhost:8270/billing-testagent
  testAgent:
    apiPath: /api/tests/
  zephyr:
    restApiUrl: https://jira.example.org/rest/atm/1.0
    zephyrEnvironment: LOCAL
    username: ${ZEPHYR_USERNAME}
    password: ${ZEPHYR_PASSWORD}
    testRunPath: /testrun
```

## Test-agent connectivity

`TestAgentWebClient` calls test agents through Spring's `RestClient` and a `SimpleClientHttpRequestFactory` with the
configured read timeout. The current code supports plain HTTP(S) access to the configured base URLs and does not contain
OAuth2 client-credentials support for outbound test-agent calls.

If a call returns a 4xx or 5xx status, or if it times out / cannot connect, the client throws `TestAgentException`.
The orchestrator then aborts the test run.

## Zephyr credentials

Provide Zephyr credentials through your secret-management mechanism or environment variables. Do not hardcode service
account credentials in source-controlled `application.yml` files.

## Persistence defaults

The library ships `jpaDefaultProperties.properties`, which sets PostgreSQL-oriented defaults such as:

- `spring.jpa.database=postgresql`
- `spring.jpa.hibernate.ddl-auto=none`
- `spring.flyway.enabled=true`
- PostgreSQL JDBC driver and dialect

A consuming service must still configure its actual datasource URL, username, password, and any pool settings.

## Related pages

- [Getting started](getting-started.md)
- [Zephyr reporting](reporting.md)
- [TestAgent API contract](test-agent-api.md)
