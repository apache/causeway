## Why

Application primers can perform substantial synchronous database work, but v4 traces do not currently identify individual callback costs.
Forward-port roadmap 10b, maintenance `473454f2447`, using the priming hooks and shared Boot/agent observation infrastructure already ported.

## What Changes

- Observe each invoked action/view primer separately as `causeway.priming.action` or `causeway.priming.view`, with no aggregate or unmatched spans.
- Use bounded domain-facing names and static canonical type/action metadata without callback implementation or payload attributes.
- Preserve deterministic order, fail-fast behavior, original failures, request admission and natural parentage/JDBC descendants.
- Retain no-op/absent-integration behavior and add focused and exported-trace regressions plus documentation.

## Capabilities

### New Capabilities

- `application-primer-observation`: Per-callback semantic observations, canonical private metadata, natural ancestry and unchanged failure/inactive behavior.

### Modified Capabilities

None.
Priming registration, callback lifecycle and public APIs remain unchanged.

## Impact

Changes are centered on `PrimingRegistryDefault` and its immutable registration metadata, using the existing integration and robust v4 observation closure.
V4 canonical action metadata retains the resolved parameter signature rather than maintenance's placeholder `()`; contextual names remain signature-free.
No public SPI, production dependency, SDK, exporter or configuration is added.
This branch is stacked on priming-hook PR #3912 and can be rebased after it merges.
