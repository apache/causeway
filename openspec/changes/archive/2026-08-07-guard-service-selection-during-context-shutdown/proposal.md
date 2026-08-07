## Why

Causeway clears its Spring context holder on `ContextClosedEvent`, before Spring invokes managed-bean destruction callbacks. Metamodel disposal subsequently performs a service lookup and currently dereferences the cleared holder, producing a swallowed `NullPointerException` during every affected context shutdown and leaving disposal incomplete.

## What Changes

- Make service selection return an empty result when the Spring context holder is unavailable during shutdown.
- Preserve normal service-selection behavior while the application context is available.
- Add focused regression coverage for selection without an available context.
- Verify an integration-test context can close without the `SpecificationLoaderDefault` destroy-method exception.
- Resolve the CAUSEWAY-4002 lifecycle question in the maintenance-branch reconciliation ledger.

## Capabilities

### New Capabilities

- `service-registry-lifecycle-safety`: Defines service-selection behavior while the Spring application context is available and after its holder has been cleared for shutdown.

### Modified Capabilities

None.

## Impact

- `core/metamodel`: `ServiceRegistryDefault` selection behavior and focused tests in `core/mmtest`.
- Spring application-context shutdown and `SpecificationLoaderDefault.disposeMetaModel()` teardown.
- Maintenance-branch reconciliation evidence for CAUSEWAY-4002.
- No public API signature, dependency, configuration, or persisted-data changes.
