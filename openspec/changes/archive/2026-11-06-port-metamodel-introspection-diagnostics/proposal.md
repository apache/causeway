## Why

Stack overflows during nested metamodel introspection currently expose framework call stacks without the active domain-type chain needed to diagnose recursion.
Forward-port CAUSEWAY-4060, maintenance `fb28fa67e06`, adapting it to v4's relocated loader, request/state handlers and existing phased mixin bootstrap.

## What Changes

- Retain a thread-local active loading chain and report it best-effort on stack overflow before rethrowing the original error.
- Include requested types, substituted types when different, requested/effective introspection requests, available current-state snapshots and compact caller locations.
- Suppress duplicate reports while the same error unwinds and clean up per-thread diagnostic state after success or failure.
- Sort registered mixin traversal by fully qualified class name without changing applicability or explicit member ordering.
- Add focused loader/diagnostic/registry tests and repeated representative metamodel bootstrap coverage, plus operational guidance and measured diagnostic overhead evidence.

## Capabilities

### New Capabilities

- `metamodel-introspection-diagnostics`: Retained error chains, thread isolation, deterministic mixin traversal and representative recursion-path regression coverage.

### Modified Capabilities

None.

## Impact

Production changes are limited to the internal loader path, a package-private diagnostic helper and the registry's mixin stream ordering.
Existing v4 state handlers/facade metadata expose sufficient package-private state; no new public introspection-state API is needed.
No loader algorithm, synchronization, cache, alias registration, observation/export configuration or dependency is changed.
Mixin applicability prefiltering (4061) remains a separate follow-up.
This branch is stacked on primer-observation PR #3913 and can be rebased after it merges.
