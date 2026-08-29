## ADDED Requirements

### Requirement: Current-platform integration-test compilation
The execution-outbox REST client integration-test sources SHALL compile against the repository's current Causeway and Spring Boot APIs without relying on removed types or obsolete package locations.

#### Scenario: Test sources are compiled explicitly
- **WHEN** Maven runs `test-compile` for the module with test skipping disabled
- **THEN** `RestEndpointService` resolves the current Restful Objects path contract
- **AND** the integration test resolves current `EntityScan` and `LocalServerPort` annotations
- **AND** no source references `RestEasyConfiguration`

#### Scenario: Further stale APIs are exposed
- **WHEN** forced test compilation reveals another removed or relocated platform API
- **THEN** the test source migrates to the current supported API
- **AND** compilation is not bypassed through exclusion, reflection, or another skip property

### Requirement: Canonical configured REST endpoint
The integration-test endpoint helper SHALL construct the `OutboxClient` base URI from the random server port, current web application context path, and canonical configured Restful Objects path.
It MUST preserve the client contract of one usable trailing path separator without hard-coding the default endpoint.

#### Scenario: Default Restful Objects path is used
- **WHEN** the test application uses the default Restful Objects base path
- **THEN** the client targets that path beneath the current web context
- **AND** outbox calls do not depend on removed RESTEasy configuration

#### Scenario: Restful Objects path is configured
- **WHEN** the test application supplies a non-default Restful Objects base path
- **THEN** the helper derives the client URI from that configured path
- **AND** does not silently fall back to `/restful`

#### Scenario: Application context path is configured
- **WHEN** the web application has a non-empty context path
- **THEN** the endpoint URI contains both the context path and Restful Objects path exactly once
- **AND** remains a valid absolute client base URI

### Requirement: Default Maven lifecycle participation
The execution-outbox REST client module SHALL compile and execute its integration tests through the ordinary Maven lifecycle by default.
Repository POM configuration MUST NOT set `maven.test.skip` or an equivalent default suppression for this suite.

#### Scenario: Module test lifecycle runs
- **WHEN** a developer runs the module's ordinary Maven test or verify lifecycle without skip flags
- **THEN** test sources compile
- **AND** the execution-outbox REST client integration tests execute

#### Scenario: Root reactor runs
- **WHEN** the module participates in the repository's ordinary reactor build
- **THEN** its test compilation and execution failures fail the build
- **AND** stale test sources cannot remain hidden behind a successful reactor result

#### Scenario: Developer explicitly skips tests
- **WHEN** a developer supplies a standard Maven command-line test-skip option
- **THEN** Maven retains its standard explicit skip behavior
- **AND** repository defaults remain test-enabled for subsequent builds

### Requirement: Restored outbox delivery regression coverage
The re-enabled integration suite SHALL preserve verification of execution-outbox recording and REST-client delivery through the running application.
Fixes MUST retain authorization, transaction, ordering, and cleanup assertions unless an explicit specification change supersedes them.

#### Scenario: Recorded interactions are delivered
- **WHEN** the test creates publishable interactions and invokes the REST outbox client with valid credentials
- **THEN** the client receives and acknowledges the expected outbox entries
- **AND** persisted entry state reflects successful delivery according to the existing contract

#### Scenario: Runtime setup fails
- **WHEN** application startup, persistence, authentication, endpoint routing, serialization, or cleanup no longer satisfies the existing test contract
- **THEN** the focused integration test fails with a diagnosable error
- **AND** the implementation corrects the deterministic regression rather than disabling the suite

#### Scenario: Suite completes
- **WHEN** the focused integration suite finishes successfully
- **THEN** created domain and outbox state is isolated or cleaned according to the existing fixture lifecycle
- **AND** repeated module runs do not depend on stale state or execution order
