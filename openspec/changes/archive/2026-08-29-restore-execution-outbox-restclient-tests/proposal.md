## Why

The execution-outbox REST client integration-test module currently skips test compilation and execution, but its dormant sources should still remain compatible with current Causeway and Spring Boot APIs.
Forced test compilation exposes source breakage caused by the RESTEasy configuration removal and Spring Boot package migrations.

## What Changes

- Replace the removed `RestEasyConfiguration` dependency in the integration-test endpoint helper with the current Restful Objects path configuration contract.
- Update stale Spring Boot test imports, including `EntityScan` and `LocalServerPort`, to their current packages.
- Force the integration-test sources to compile during implementation and resolve compile-time migration failures without changing their assertions.
- Retain the module's existing test suppression and do not restore runtime execution of the dormant integration suite in this change.

## Capabilities

### New Capabilities

- `execution-outbox-restclient-integration-tests`: Defines source-compatibility requirements for the currently dormant execution-outbox REST client integration suite.

### Modified Capabilities

None.

## Impact

The change affects only test sources under `extensions/core/executionoutbox/restclient`.
It uses the existing `RestfulPathProvider` API and current Spring Boot test annotations without changing production REST client behavior, endpoint behavior, outbox persistence semantics, or the module's existing test-execution policy.
