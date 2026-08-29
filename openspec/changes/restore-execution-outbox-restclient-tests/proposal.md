## Why

The execution-outbox REST client integration-test module has skipped all test compilation and execution since July 2025, masking source breakage caused by the RESTEasy configuration removal and Spring Boot package migrations.
Restoring this suite is necessary so normal reactor builds verify that outbox entries can still be delivered through the configured Restful Objects endpoint.

## What Changes

- Replace the removed `RestEasyConfiguration` dependency in the integration-test endpoint helper with the current Restful Objects path configuration contract.
- Update stale Spring Boot test imports, including `EntityScan` and `LocalServerPort`, to their current packages.
- Force the integration-test sources to compile during implementation and resolve any additional compile-time migration failures exposed by doing so.
- Run and repair the execution-outbox REST client integration tests while preserving their original delivery, authentication, transaction, and cleanup intent.
- Remove the module-level `maven.test.skip` suppression after the suite compiles and passes, so ordinary module and reactor builds exercise it.
- Add focused regression coverage or build assertions where needed to prevent test-source compilation from being silently disabled again.

## Capabilities

### New Capabilities

- `execution-outbox-restclient-integration-tests`: Defines compilation, endpoint-path configuration, execution, and reactor participation requirements for the execution-outbox REST client integration suite.

### Modified Capabilities

None.

## Impact

The change affects `extensions/core/executionoutbox/restclient` test sources and Maven configuration.
It uses the existing `CausewayConfiguration` and `RestfulPathProvider` APIs and current Spring Boot test annotations without changing production REST client APIs, endpoint defaults, authentication behavior, or outbox persistence semantics.
Normal builds may take longer because the previously suppressed integration suite will execute again.
