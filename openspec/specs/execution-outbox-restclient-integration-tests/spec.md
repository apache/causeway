# Execution Outbox REST Client Integration Tests Specification

## Purpose

Define source-compatibility expectations for the currently dormant execution-outbox REST client integration suite while preserving its existing execution policy.

## Requirements

### Requirement: Current-platform integration-test source compatibility
The dormant execution-outbox REST client integration-test sources SHALL compile against the repository's current Causeway and Spring Boot APIs when test compilation is explicitly enabled.
They SHALL NOT rely on removed types or obsolete package locations.

#### Scenario: Test sources are compiled explicitly
- **WHEN** Maven runs `test-compile` for the module with `-Dmaven.test.skip=false`
- **THEN** `RestEndpointService` resolves the current Restful Objects path contract
- **AND** the integration test resolves current `EntityScan` and `LocalServerPort` annotations
- **AND** no source references `RestEasyConfiguration`

#### Scenario: Further stale APIs are exposed
- **WHEN** forced test compilation reveals another removed or relocated platform API
- **THEN** the test source migrates to the current supported API
- **AND** compilation is not bypassed through source exclusion, reflection, or deletion

### Requirement: Existing execution policy remains unchanged
This compilation-only change SHALL NOT re-enable or repair runtime execution of the dormant integration suite.
It SHALL NOT change production outbox REST behavior as a side effect of source migration.

#### Scenario: Normal module lifecycle runs
- **WHEN** Maven executes the module with its existing repository configuration
- **THEN** the existing test-suppression policy remains unchanged
- **AND** production `OutboxClient`, `OutboxEvents`, endpoint behavior, and persistence semantics remain unchanged

#### Scenario: Runtime restoration is requested later
- **WHEN** maintainers choose to restore execution of the integration suite
- **THEN** runtime diagnosis, production fixes, lifecycle participation, and repeated-run coverage are handled by a separately scoped change
