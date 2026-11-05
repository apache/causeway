## Why

Lazy property-change evaluation can execute expensive derived accessors and SQL without a semantic trace boundary identifying its aggregate cost.
Forward-port maintenance CAUSEWAY-4059 commit `02786054574` as roadmap step 8b, using v4's existing shared Boot/agent integration.

## What Changes

- Add one `causeway.entitychange.evaluate` observation, displayed as `evaluate property changes`, around nonempty candidate evaluation and its existing defensive retry.
- Preserve empty-candidate bypass, unknown-value handling, original escaping failures, lazy memoization and publication behavior.
- Keep synchronous JDBC descendants under natural current context, with evaluation complete before audit/publication callbacks.
- Add focused and exported-trace tests, private static metadata, documentation and roadmap evidence.

## Capabilities

### New Capabilities

- `entity-change-evaluation-observation`: Aggregate candidate evaluation observations with bounded metadata, natural ancestry and unchanged retry/failure/publication semantics.

### Modified Capabilities

None.

## Impact

`EntityChangeTrackerDefault` gains constructor-injected `CausewayObservationIntegration`; explicit construction sites require updating.
The integration is already available through existing module dependencies, so no production library, SDK, exporter or configuration is added.
Existing transaction, execution publishing, audit and JPA observations remain distinct and unchanged.
This branch is stacked on the audit forward port in PR #3897 and can be rebased after that PR merges.
Application-defined spans and priming remain outside scope.
